package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress
import java.nio.ByteOrder

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.util.ByteString
import net.fehmicansaglam.tepkin.RetryStrategy.FixedRetryStrategy
import net.fehmicansaglam.tepkin.TepkinMessages._
import net.fehmicansaglam.tepkin.protocol.message.{Message, Reply}

class MongoConnection(manager: ActorRef, remote: InetSocketAddress, retryStrategy: RetryStrategy)
  extends Actor
  with ActorLogging {

  import context.dispatcher

  var requests = Map.empty[Int, ActorRef]
  var storage: ByteString = null
  var retries = 0
  var shuttingDown = false

  manager ! Connect(remote)

  def receive: Receive = {
    case CommandFailed(_: Connect) =>
      log.info("Connection failed.")
      retry()

    case Connected(remote, local) =>
      log.info(s"Connected to $remote")
      retries = 0
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
      shuttingDown = true
      connection ! Close

    case _: ConnectionClosed =>
      log.info("Connection closed.")
      if (shuttingDown) {
        context.parent ! ConnectionClosed
        context stop self
      } else {
        retry()
      }
  }

  def buffering(connection: ActorRef, expectedSize: Int): Receive = {
    case r@Received(data) =>
      storage ++= data
      if (expectedSize == storage.size) {
        context become working(connection)
        self ! Received(storage)
      }

    case _: ConnectionClosed =>
      log.info("Connection closed.")
      retry()
  }

  private def retry() = {
    if (retries == retryStrategy.maxRetries) {
      log.info("Max retry count has been reached. Giving up.")
      context.parent ! ConnectFailed
      context stop self
    } else {
      retries += 1
      log.info(s"Retrying to connect for the $retries. time.")
      context.system.scheduler.scheduleOnce(retryStrategy.nextDelay(retries), manager, Connect(remote))
      context become receive
    }
  }

}

object MongoConnection {
  def props(manager: ActorRef, remote: InetSocketAddress, retryStrategy: RetryStrategy = FixedRetryStrategy()): Props = {
    Props(classOf[MongoConnection], manager, remote, retryStrategy)
  }
}
