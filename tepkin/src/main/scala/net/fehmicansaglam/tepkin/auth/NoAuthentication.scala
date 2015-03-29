package net.fehmicansaglam.tepkin.auth

import akka.actor.{Actor, ActorLogging, ActorRef}
import net.fehmicansaglam.tepkin.MongoCredentials
import net.fehmicansaglam.tepkin.TepkinMessage.Idle

trait NoAuthentication extends Authentication {
  this: Actor with ActorLogging =>

  override def authenticate(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Unit = {
    context.become(working(connection))
    context.parent ! Idle
  }

}
