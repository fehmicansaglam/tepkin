package net.fehmicansaglam.tepkin.auth

import akka.actor.Actor.Receive
import akka.actor.ActorRef
import net.fehmicansaglam.tepkin.MongoCredentials

trait Authentication {

  def working(connection: ActorRef): Receive

  def authenticate(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Unit
}
