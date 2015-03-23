package net.fehmicansaglam.tepkin


import akka.actor.{ActorRefFactory, ActorSystem}
import net.fehmicansaglam.tepkin.TepkinMessage.ShutDown
import net.fehmicansaglam.tepkin.protocol.ReadPreference

import scala.concurrent.ExecutionContext

class MongoClient(val context: ActorRefFactory, uri: MongoClientUri, nConnectionsPerNode: Int = 10) {
  val poolManager = context.actorOf(
    MongoPoolManager
      .props(uri.hosts, nConnectionsPerNode, uri.option("readPreference").map(ReadPreference.apply))
      .withMailbox("tepkin-mailbox"),
    name = "tepkin-pool")

  implicit def ec: ExecutionContext = context.dispatcher

  def apply(databaseName: String): MongoDatabase = {
    require(databaseName != null && databaseName.getBytes("UTF-8").size < 123,
      "Database name must be shorter than 123 bytes")
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
