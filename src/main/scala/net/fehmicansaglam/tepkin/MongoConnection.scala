package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import net.fehmicansaglam.tepkin.TepkinMessages._
import net.fehmicansaglam.tepkin.protocol.message.{Message, Reply}

class MongoConnection(manager: ActorRef, remote: InetSocketAddress) extends Actor {

  var requests = Map.empty[Int, ActorRef]

  manager ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      context.parent ! ConnectFailed
      context stop self

    case Connected(remote, local) =>
      val connection = sender()
      connection ! Register(self)
      context.become(working(connection))
      context.parent ! Idle
  }

  def working(connection: ActorRef): Receive = {
    case m: Message =>
      requests += (m.requestID -> sender())
      connection ! Write(m.encode())

    case CommandFailed(w: Write) =>
      // O/S buffer was full
      context.parent ! WriteFailed

    case Received(data) =>
      Reply.decode(data.asByteBuffer) foreach { reply =>
        requests.get(reply.responseTo) foreach { request =>
          request ! reply
        }
      }
      context.parent ! Idle

    case ShutDown =>
      connection ! Close

    case _: ConnectionClosed =>
      context.parent ! ConnectionClosed
      context stop self
  }
}

object MongoConnection {
  def props(manager: ActorRef, remote: InetSocketAddress): Props = {
    Props(classOf[MongoConnection], manager, remote)
  }
}
