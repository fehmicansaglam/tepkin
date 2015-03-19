package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import net.fehmicansaglam.tepkin.TepkinMessage.{ShutDown, WhatsYourVersion}
import net.fehmicansaglam.tepkin.protocol.MongoWireVersion
import net.fehmicansaglam.tepkin.protocol.command.IsMaster
import net.fehmicansaglam.tepkin.protocol.message.Reply
import net.fehmicansaglam.tepkin.protocol.result.IsMasterResult

import scala.collection.mutable
import scala.concurrent.duration._

class MongoPoolManager(seeds: Set[InetSocketAddress], nConnectionsPerNode: Int)
  extends Actor
  with ActorLogging {

  case object PingNodes

  case class PingNodesResult(result: IsMasterResult, elapsed: Int)

  case class NodeEntry(remote: InetSocketAddress, pool: ActorRef, maxWireVersion: Int, var elapsed: Int)

  import context.dispatcher

  var pools = Set.empty[ActorRef]
  var nodes = seeds
  var primary: Option[ActorRef] = None
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

  context.system.scheduler.schedule(initialDelay = 0.seconds, interval = 10.seconds, self, PingNodes)

  def receive = {

    case PingNodes =>
      pingNodes()

    case PingNodesResult(result, elapsed) =>
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
        log.info("New node found. Created pool for {}", node)
        pools += pool
      }

      nodes ++= newNodes

      if (result.isMaster) {
        primary match {
          case Some(ref) if ref == sender() =>
          // I already know the primary.
          case Some(ref) if ref != sender() =>
            log.info("Primary has changed. New primary {}, maxWireVersion {}", sender(), result.maxWireVersion)
            primary = Some(sender())
            maxWireVersion = result.maxWireVersion
          case None =>
            log.info("Found primary {}, maxWireVersion {}", sender(), result.maxWireVersion)
            primary = Some(sender())
            maxWireVersion = result.maxWireVersion
            stash foreach { case (ref, message) =>
              self.tell(message, ref)
            }
        }
      }

    case WhatsYourVersion =>
      sender() ! maxWireVersion

    case ShutDown =>
      pools foreach (_ ! ShutDown)

    case message =>
      primary match {
        case Some(ref) => ref forward message
        case None => stash.enqueue((sender(), message))
      }
  }

  private def pingNodes(): Unit = {
    pools foreach { pool =>
      implicit val timeout: Timeout = 10.seconds
      val begin = System.currentTimeMillis()
      (pool ? IsMaster).mapTo[Reply].map { reply =>
        val elapsed = System.currentTimeMillis() - begin
        PingNodesResult(IsMasterResult(reply.documents.head), elapsed.toInt)
      }.pipeTo(self)(pool)
    }
  }
}

object MongoPoolManager {
  def props(seeds: Set[InetSocketAddress], nConnectionsPerNode: Int): Props = {
    Props(classOf[MongoPoolManager], seeds, nConnectionsPerNode)
  }
}
