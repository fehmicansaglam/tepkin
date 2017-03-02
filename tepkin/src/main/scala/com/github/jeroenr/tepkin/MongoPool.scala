package com.github.jeroenr.tepkin

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}
import com.github.jeroenr.tepkin.TepkinMessage.{Idle, Init, ShutDown}
import com.github.jeroenr.tepkin.protocol.AuthMechanism
import com.github.jeroenr.tepkin.protocol.message.Message

import scala.collection.mutable

/**
 * Manages a connection pool to a single MongoDB instance.
 *
 * @param remote MongoDB instance remote address
 * @param poolSize Number of connections.
 * @param databaseName Authentication database
 * @param credentials Optional. Authentication credentials.
 * @param authMechanism Optional. Authentication mechanism.
 */
class MongoPool(remote: InetSocketAddress,
                poolSize: Int,
                databaseName: String,
                credentials: Option[MongoCredentials],
                authMechanism: Option[AuthMechanism]) extends Actor {

  import context.system

  val manager = IO(Tcp)
  var idleConnections = Set.empty[ActorRef]
  val stash = mutable.Queue.empty[(ActorRef, Message)]

  self ! Init

  def receive: Receive = {

    // Create initial pool of connections.
    case Init =>
      (0 until poolSize) foreach { i =>
        context.watch {
          context.actorOf(
            MongoConnection.props(manager, remote, databaseName, credentials, authMechanism),
            s"connection-$i"
          )
        }
      }

    // First connection has been established.
    case Idle =>
      idleConnections += sender()
      context.become(working)
      if (stash.nonEmpty) {
        val item = stash.dequeue()
        self.tell(item._2, item._1)
      }

    case message: Message => stash.enqueue((sender(), message))
  }

  def working: Receive = {

    // Sender has finished its task and is idle.
    case Idle =>
      idleConnections += sender()

      (1 to Math.min(idleConnections.size, stash.size)) foreach { _ =>
        val item = stash.dequeue()
        idleConnections.head.tell(item._2, item._1)
        idleConnections = idleConnections.tail
      }

    case message: Message =>
      if (idleConnections.isEmpty) {
        stash.enqueue((sender(), message))
      } else {
        idleConnections.head.forward(message)
        idleConnections = idleConnections.tail
      }

    case ShutDown =>
      context.become(shuttingDown)
      if (stash.isEmpty) {
        idleConnections foreach (_ ! ShutDown)
      }
  }

  def shuttingDown: Receive = {

    case Idle =>
      idleConnections += sender()

      (1 to Math.min(idleConnections.size, stash.size)) foreach { _ =>
        val item = stash.dequeue()
        idleConnections.head.tell(item._2, item._1)
        idleConnections = idleConnections.tail
      }

      if (stash.isEmpty) {
        idleConnections.foreach(_ ! ShutDown)
      }

    case Terminated(connection) =>
      idleConnections -= connection
      if (idleConnections.isEmpty) {
        context.stop(self)
      }
  }

  override def postStop(): Unit = {
    context.system.terminate()
    ()
  }
}

object MongoPool {
  def props(remote: InetSocketAddress,
            poolSize: Int,
            databaseName: String,
            credentials: Option[MongoCredentials] = None,
            authMechanism: Option[AuthMechanism] = None): Props = {
    Props(classOf[MongoPool], remote, poolSize, databaseName, credentials, authMechanism)
  }
}
