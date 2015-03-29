package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress
import java.nio.ByteOrder

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp._
import akka.util.ByteString
import net.fehmicansaglam.tepkin.RetryStrategy.FixedRetryStrategy
import net.fehmicansaglam.tepkin.TepkinMessage._
import net.fehmicansaglam.tepkin.auth.{Authentication, MongoDbCrAuthentication, NoAuthentication, ScramSha1Authentication}
import net.fehmicansaglam.tepkin.protocol.AuthMechanism
import net.fehmicansaglam.tepkin.protocol.message.{Message, Reply}

class MongoConnection(manager: ActorRef,
                      remote: InetSocketAddress,
                      databaseName: String,
                      credentials: Option[MongoCredentials],
                      retryStrategy: RetryStrategy)
  extends Actor
  with ActorLogging {
  this: Authentication =>

  import context.dispatcher

  var requests = Map.empty[Int, ActorRef]
  var storage: ByteString = null
  var nRetries = 0
  var shuttingDown = false

  log.debug("Connecting to {}", remote)
  manager ! Connect(remote)

  def receive: Receive = {
    case CommandFailed(_: Connect) =>
      log.info("Connection failed.")
      retry()

    case Connected(_, local) =>
      log.info("Connected to {}", remote)
      nRetries = 0
      val connection = sender()
      connection ! Register(self)
      authenticate(connection, databaseName, credentials)
  }

  def working(connection: ActorRef): Receive = {
    case m: Message =>
      log.debug("Received request {}", m)
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
      log.debug("Shutting down")
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

  override def authenticated(connection: ActorRef): Unit = {
    context.become(working(connection))
    context.parent ! Idle
  }

  private def retry() = {
    if (nRetries == retryStrategy.maxRetries) {
      log.info("Max retry count has been reached. Giving up.")
      context.parent ! ConnectFailed
      context stop self
    } else {
      nRetries += 1
      log.info("Retrying to connect for the {}. time.", nRetries)
      context.system.scheduler.scheduleOnce(retryStrategy.nextDelay(nRetries), manager, Connect(remote))
      context.become(receive)
    }
  }

}

object MongoConnection {
  def props(manager: ActorRef,
            remote: InetSocketAddress,
            databaseName: String,
            credentials: Option[MongoCredentials] = None,
            authMechanism: Option[AuthMechanism] = None,
            retryStrategy: RetryStrategy = FixedRetryStrategy()): Props = {
    Props {
      authMechanism match {
        case Some(AuthMechanism.SCRAM_SHA_1) =>
          new MongoConnection(manager, remote, databaseName, credentials, retryStrategy)
            with ScramSha1Authentication

        case Some(AuthMechanism.MONGODB_CR) =>
          new MongoConnection(manager, remote, databaseName, credentials, retryStrategy)
            with MongoDbCrAuthentication

        case _ =>
          new MongoConnection(manager, remote, databaseName, credentials, retryStrategy)
            with NoAuthentication
      }
    }
  }
}
