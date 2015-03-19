package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}
import net.fehmicansaglam.tepkin.TepkinMessage.{Idle, Init, ShutDown}
import net.fehmicansaglam.tepkin.protocol.message.Message

import scala.collection.mutable

class MongoPool(remote: InetSocketAddress, poolSize: Int)
  extends Actor {

  import context.system

  val manager = IO(Tcp)
  var idleConnections = Set.empty[ActorRef]
  val stash = mutable.Queue.empty[(ActorRef, Message)]

  self ! Init

  def receive = initing

  def initing: Receive = {

    // Create initial pool of connections.
    case Init =>
      (0 until poolSize) foreach { i =>
        context.watch {
          context.actorOf(
            MongoConnection.props(manager, remote),
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
        idleConnections foreach (_ ! ShutDown)
      }

    case Terminated(connection) =>
      idleConnections -= connection
      if (idleConnections.isEmpty) {
        context stop self
      }
  }

  override def postStop(): Unit = {
    context.system.shutdown()
  }
}

object MongoPool {
  def props(remote: InetSocketAddress, poolSize: Int): Props = {
    Props(classOf[MongoPool], remote, poolSize)
  }
}
