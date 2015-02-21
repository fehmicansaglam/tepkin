package net.fehmicansaglam.tepkin

import akka.actor.ActorSystem
import net.fehmicansaglam.tepkin.TepkinMessages.ShutDown

class MongoClient(val system: ActorSystem, host: String, port: Int) {
  val pool = system.actorOf(MongoPool.props(host, port).withMailbox("tepkin-mailbox"))

  def apply(databaseName: String, collectionName: String): MongoCollection = {
    new MongoCollection(databaseName, collectionName, system, pool)
  }

  def shutdown(): Unit = {
    pool ! ShutDown
  }
}

object MongoClient {
  def apply(host: String, port: Int)(implicit system: ActorSystem = ActorSystem("tepkinSystem")): MongoClient = {
    new MongoClient(system, host, port)
  }
}
