package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}
import net.fehmicansaglam.tepkin.TepkinMessages.{Idle, InitPool, ShutDown}
import net.fehmicansaglam.tepkin.protocol.command.Command

import scala.collection.mutable

class MongoPool(host: String, port: Int, poolSize: Int)
  extends Actor {

  import context.system

  val manager = IO(Tcp)
  val remote = new InetSocketAddress(host, port)
  var idleConnections = Set.empty[ActorRef]
  val stash = mutable.Queue.empty[(ActorRef, Command)]

  self ! InitPool

  def receive = initing

  def initing: Receive = {

    // Create initial pool of connections.
    case InitPool =>
      (0 until poolSize) foreach { i =>
        context.watch {
          context.actorOf(MongoConnection.props(manager, remote).withMailbox("tepkin-mailbox"), s"connection-$host-$port-$i")
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

    case command: Command => stash.enqueue((sender(), command))
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

    case command: Command =>
      if (idleConnections.isEmpty) {
        stash.enqueue((sender(), command))
      } else {
        idleConnections.head.forward(command)
        idleConnections = idleConnections.tail
      }

    case ShutDown => {
      context.become(shuttingDown)
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
        idleConnections foreach (_ ! PoisonPill)
      }

    case Terminated(connection) => {
      idleConnections -= connection
      if (idleConnections.isEmpty) {
        context stop self
      }
    }
  }

  override def postStop(): Unit = {
    context.system.shutdown()
  }
}

object MongoPool {
  def props(host: String, port: Int, poolSize: Int = 23): Props = {
    Props(classOf[MongoPool], host, port, poolSize)
  }
}