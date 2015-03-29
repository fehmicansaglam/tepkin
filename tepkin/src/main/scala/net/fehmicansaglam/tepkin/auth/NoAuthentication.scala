package net.fehmicansaglam.tepkin.auth

import akka.actor.{Actor, ActorLogging, ActorRef}
import net.fehmicansaglam.tepkin.MongoCredentials

trait NoAuthentication extends Authentication {
  this: Actor with ActorLogging =>

  override def authenticate(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Unit = {
    authenticated(connection)
  }

}
