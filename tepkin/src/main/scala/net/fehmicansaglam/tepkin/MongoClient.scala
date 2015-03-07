package net.fehmicansaglam.tepkin


import akka.actor.{ActorRefFactory, ActorSystem}
import net.fehmicansaglam.tepkin.TepkinMessages.ShutDown

import scala.concurrent.ExecutionContext

class MongoClient(val context: ActorRefFactory, uri: MongoClientUri, nConnectionsPerNode: Int = 10) {
  val poolManager = context.actorOf(MongoPoolManager.props(uri.hosts, nConnectionsPerNode)
    .withMailbox("tepkin-mailbox"), name = "tepkin-pool")

  implicit def ec: ExecutionContext = context.dispatcher

  def apply(databaseName: String): MongoDatabase = {
    new MongoDatabase(poolManager, databaseName)
  }

  def db(databaseName: String): MongoDatabase = {
    apply(databaseName)
  }

  def shutdown(): Unit = {
    poolManager ! ShutDown
  }
}

object MongoClient {

  def apply(uri: String, context: ActorRefFactory = ActorSystem("tepkin-system")): MongoClient = {
    new MongoClient(context, MongoClientUri(uri))
  }

}
