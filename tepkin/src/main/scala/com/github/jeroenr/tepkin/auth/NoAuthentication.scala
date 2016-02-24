package com.github.jeroenr.tepkin.auth

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.github.jeroenr.tepkin.MongoCredentials

trait NoAuthentication extends Authentication {
  this: Actor with ActorLogging =>

  override def authenticate(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Unit = {
    authenticated(connection)
  }

}
