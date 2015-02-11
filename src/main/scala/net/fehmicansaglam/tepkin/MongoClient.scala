package net.fehmicansaglam.tepkin

import akka.actor.{ActorSystem, Props}

class MongoClient(system: ActorSystem, host: String, port: Int) {

  val pool = system.actorOf(Props(classOf[MongoPool], host, port))

  def apply(databaseName: String, collectionName: String): MongoCollection = {
    new MongoCollection(databaseName, collectionName, pool)
  }
}

object MongoClient {
  def apply(host: String, port: Int)(implicit system: ActorSystem = ActorSystem("tepkinSystem")): MongoClient = {
    new MongoClient(system, host, port)
  }
}
