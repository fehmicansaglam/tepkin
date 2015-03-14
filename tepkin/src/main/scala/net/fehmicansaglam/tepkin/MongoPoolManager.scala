package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.ask
import akka.util.Timeout
import net.fehmicansaglam.tepkin.TepkinMessages.{Init, ShutDown, WhatsYourVersion}
import net.fehmicansaglam.tepkin.protocol.MongoWireVersion
import net.fehmicansaglam.tepkin.protocol.command.IsMaster
import net.fehmicansaglam.tepkin.protocol.message.{Message, Reply}
import net.fehmicansaglam.tepkin.protocol.result.IsMasterResult

import scala.collection.mutable
import scala.concurrent.duration._

class MongoPoolManager(seeds: Set[InetSocketAddress], nConnectionsPerNode: Int)
  extends Actor
  with ActorLogging {

  import context.dispatcher

  var pools = Set.empty[ActorRef]
  var nodes = seeds
  var primary: ActorRef = null
  var maxWireVersion: Int = MongoWireVersion.v26
  val stash = mutable.Queue.empty[(ActorRef, Any)]

  seeds foreach { seed =>
    val pool = context.actorOf(
      MongoPool.props(seed, nConnectionsPerNode),
      s"pool-$seed".replaceAll("\\W", "_")
    )
    log.info("Created pool for {}", seed)
    pools += pool
  }

  self ! Init

  def receive = {

    case Init =>
      implicit val timeout: Timeout = 10.seconds
      pools foreach { pool =>
        (pool ? IsMaster).mapTo[Reply].map { reply =>
          val result = IsMasterResult(reply.documents(0))
          self.tell(result, pool)
        }
      }

    case result: IsMasterResult =>
      val newNodes = result.replicaSet.map {
        _.hosts.map { node =>
          val Array(host, port) = node.split(":")
          new InetSocketAddress(host, port.toInt)
        }.toSet
      }.getOrElse(Set.empty)

      (newNodes diff nodes) foreach { node =>
        val pool = context.actorOf(
          MongoPool.props(node, nConnectionsPerNode),
          s"pool-$node".replaceAll("\\W", "_")
        )
        log.info("Created pool for {}", node)
        pools += pool

        implicit val timeout: Timeout = 10.seconds
        (pool ? IsMaster).mapTo[Reply].map { reply =>
          val result = IsMasterResult(reply.documents(0))
          self.tell(result, pool)
        }
      }

      nodes ++= newNodes

      if (result.isMaster) {
        primary = sender()
        maxWireVersion = result.maxWireVersion
        log.info("Found primary {}, maxWireVersion {}", primary, maxWireVersion)
        context become working
        stash foreach { case (ref, message) =>
          self.tell(message, ref)
        }
      }

    case message =>
      stash.enqueue((sender(), message))
  }

  def working: Receive = {
    case message: Message =>
      primary forward message

    case WhatsYourVersion =>
      sender() ! maxWireVersion

    case ShutDown =>
      pools foreach (_ ! ShutDown)
  }
}

object MongoPoolManager {
  def props(seeds: Set[InetSocketAddress], nConnectionsPerNode: Int): Props = {
    Props(classOf[MongoPoolManager], seeds, nConnectionsPerNode)
  }
}
