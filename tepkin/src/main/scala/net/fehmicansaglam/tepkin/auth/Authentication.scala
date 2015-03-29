package net.fehmicansaglam.tepkin.auth

import akka.actor.ActorRef
import net.fehmicansaglam.tepkin.MongoCredentials

trait Authentication {

  def authenticate(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Unit

  def authenticated(connection: ActorRef): Unit
}
