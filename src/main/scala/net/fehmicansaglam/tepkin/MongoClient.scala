package net.fehmicansaglam.tepkin

import akka.actor.{ActorRefFactory, ActorSystem}
import net.fehmicansaglam.tepkin.TepkinMessages.ShutDown

class MongoClient(val context: ActorRefFactory, host: String, port: Int) {
  val pool = context.actorOf(MongoPool.props(host, port).withMailbox("tepkin-mailbox"))

  def apply(databaseName: String, collectionName: String): MongoCollection = {
    new MongoCollection(databaseName, collectionName, pool)
  }

  def shutdown(): Unit = {
    pool ! ShutDown
  }
}

object MongoClient {
  def apply(host: String, port: Int, context: ActorRefFactory = ActorSystem("tepkin-system")): MongoClient = {
    new MongoClient(context, host, port)
  }
}
