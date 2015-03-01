package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress
import java.nio.ByteOrder

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.util.ByteString
import net.fehmicansaglam.tepkin.TepkinMessages._
import net.fehmicansaglam.tepkin.protocol.message.{Message, Reply}

class MongoConnection(manager: ActorRef, remote: InetSocketAddress)
  extends Actor
  with ActorLogging {

  var requests = Map.empty[Int, ActorRef]
  var storage: ByteString = null

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
      val buffer = data.asByteBuffer
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      val expectedSize = buffer.getInt
      buffer.rewind()

      if (expectedSize > buffer.remaining()) {
        storage = data
        context become buffering(connection, expectedSize)
      } else {
        Reply.decode(data.asByteBuffer) foreach { reply =>
          requests.get(reply.responseTo) foreach { request =>
            log.debug("Received reply for request {}", reply.responseTo)
            request ! reply
            requests -= reply.responseTo
          }
        }
        storage = null
        context.parent ! Idle
      }

    case ShutDown =>
      connection ! Close

    case _: ConnectionClosed =>
      context.parent ! ConnectionClosed
      context stop self
  }

  def buffering(connection: ActorRef, expectedSize: Int): Receive = {
    case r@Received(data) =>
      storage ++= data
      if (expectedSize == storage.size) {
        context become working(connection)
        self ! Received(storage)
      }
  }
}

object MongoConnection {
  def props(manager: ActorRef, remote: InetSocketAddress): Props = {
    Props(classOf[MongoConnection], manager, remote)
  }
}
