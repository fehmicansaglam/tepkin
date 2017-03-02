package com.github.jeroenr.tepkin.auth

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp.{Received, Write}
import com.github.jeroenr.tepkin.MongoCredentials
import com.github.jeroenr.tepkin.protocol.command.{Authenticate, GetNonce}
import com.github.jeroenr.tepkin.protocol.message.Reply

trait MongoDbCrAuthentication extends Authentication {
  this: Actor with ActorLogging =>

  override def authenticate(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Unit = {
    log.info("Authenticating to {}", databaseName)
    context.become(noncing(connection, databaseName, credentials))
    connection ! Write(GetNonce(databaseName).encode)
  }

  def noncing(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Receive = {
    // Received nonce
    case Received(data) =>
      for {
        reply <- Reply(data.asByteBuffer)
        nonce <- reply.documents.head.getAs[String]("nonce")
        credentials <- credentials
      } {
        log.debug("Received nonce: {}", nonce)
        context.become(authenticating(connection))
        val authenticate = Authenticate(
          databaseName,
          credentials.username,
          credentials.password.getOrElse(""),
          nonce
        )
        connection ! Write(authenticate.encode)
      }
  }

  def authenticating(connection: ActorRef): Receive = {
    // Received authentication result
    case Received(data) =>
      for {
        reply <- Reply(data.asByteBuffer)
        result <- reply.documents.headOption
      } {
        log.info("Authentication result: {}", result)
        authenticated(connection)
      }
  }

}
