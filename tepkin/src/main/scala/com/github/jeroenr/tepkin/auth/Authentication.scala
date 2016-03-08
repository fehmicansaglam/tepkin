package com.github.jeroenr.tepkin.auth

import akka.actor.ActorRef
import com.github.jeroenr.tepkin.MongoCredentials

trait Authentication {

  def authenticate(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Unit

  def authenticated(connection: ActorRef): Unit
}
