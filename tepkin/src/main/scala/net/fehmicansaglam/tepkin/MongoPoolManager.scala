package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import net.fehmicansaglam.tepkin.TepkinMessage.{CursorClosed, CursorOpened, ShutDown, WhatsYourVersion}
import net.fehmicansaglam.tepkin.protocol.command.IsMaster
import net.fehmicansaglam.tepkin.protocol.message.Reply
import net.fehmicansaglam.tepkin.protocol.result.IsMasterResult
import net.fehmicansaglam.tepkin.protocol.{MongoWireVersion, ReadPreference}

import scala.collection.mutable
import scala.concurrent.duration._

class MongoPoolManager(uri: MongoClientUri, nConnectionsPerNode: Int, readPreference: ReadPreference)
  extends Actor
  with ActorLogging {

  case object PingNodes

  case class PingNodesResult(result: IsMasterResult, elapsed: Int)

  case class NodeDetails(maxWireVersion: Int, elapsed: Int)

  import context.dispatcher

  var remotes = uri.hosts
  val nodes = mutable.Map.empty[ActorRef, NodeDetails]
  var pools = Set.empty[ActorRef]
  var primary: Option[ActorRef] = None
  var maxWireVersion: Int = MongoWireVersion.v26
  val stash = mutable.Queue.empty[(ActorRef, Any)]
  val cursors = mutable.Map.empty[Long, ActorRef]

  for (remote <- remotes) {
    val pool = context.actorOf(
      MongoPool.props(
        remote,
        uri.database.getOrElse("admin"),
        uri.credentials,
        nConnectionsPerNode),
      s"pool-$remote".replaceAll("\\W", "_"))
    log.info("Created pool for {}", remote)
    pools += pool
  }

  val pinger = context.system.scheduler.schedule(initialDelay = 0.seconds, interval = 10.seconds, self, PingNodes)

  def receive = {

    case PingNodes =>
      pingNodes()

    case PingNodesResult(result, elapsed) =>
      nodes += (sender() -> NodeDetails(result.maxWireVersion, elapsed))
      log.debug(s"Nodes: $nodes")

      val newRemotes = result.replicaSet.map { replicaSet =>
        replicaSet.hosts.map { node =>
          val Array(host, port) = node.split(":")
          new InetSocketAddress(host, port.toInt)
        }.toSet
      }.getOrElse(Set.empty)

      for (remote <- newRemotes.diff(remotes)) {
        val pool = context.actorOf(
          MongoPool.props(
            remote,
            uri.database.getOrElse("admin"),
            uri.credentials,
            nConnectionsPerNode),
          s"pool-$remote".replaceAll("\\W", "_"))
        log.info("New node found. Created pool for {}", remote)
        pools += pool
      }

      remotes ++= newRemotes

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
      pinger.cancel()
      pools foreach (_ ! ShutDown)

    case CursorOpened(cursorID) =>
      cursors += cursorID -> sender()
      log.debug("Cursor {} opened.", cursorID)

    case CursorClosed(cursorID) =>
      cursors -= cursorID
      log.debug("Cursor {} closed.", cursorID)

    case message =>
      primary match {
        case Some(ref) => ref forward message
        case None => stash.enqueue((sender(), message))
      }
  }

  private def pingNodes(): Unit = {
    for (pool <- pools) {
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
  def props(uri: MongoClientUri,
            nConnectionsPerNode: Int,
            readPreference: Option[ReadPreference] = None): Props = {
    Props(classOf[MongoPoolManager], uri, nConnectionsPerNode, readPreference.getOrElse(ReadPreference.Primary))
  }
}
