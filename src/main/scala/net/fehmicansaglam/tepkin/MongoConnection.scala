package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp._
import net.fehmicansaglam.tepkin.TepkinMessages.Idle
import net.fehmicansaglam.tepkin.protocol.message.{Message, Reply}


class MongoConnection(manager: ActorRef, remote: InetSocketAddress) extends Actor {

  var requests = Map.empty[Int, ActorRef]

  manager ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) =>
      context.parent ! "connect failed"
      context stop self

    case Connected(remote, local) =>
      context.parent ! Idle
      val connection = sender()
      connection ! Register(self)
      context become {
        case m: Message =>
          requests += (m.requestID -> sender())
          connection ! Write(m.encode())
        //          println(s"Sent message $m ${m.requestID}")

        case CommandFailed(w: Write) =>
          // O/S buffer was full
          context.parent ! "write failed"

        case Received(data) =>
          Reply.decode(data.asByteBuffer) foreach { reply =>
            //            println(s"Received reply $reply")
            //            println(self)
            requests.get(reply.responseTo) foreach { request =>
              request ! reply
            }
          }
          context.parent ! Idle

        case "close" =>
          connection ! Close

        case _: ConnectionClosed =>
          context.parent ! "connection closed"
          context stop self
      }
  }
}

object MongoConnection {
  def props(manager: ActorRef, remote: InetSocketAddress): Props = {
    Props(classOf[MongoConnection], manager, remote)
  }
}
