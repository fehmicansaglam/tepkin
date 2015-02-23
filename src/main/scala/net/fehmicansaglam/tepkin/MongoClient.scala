package net.fehmicansaglam.tepkin

import akka.actor.{ActorRefFactory, ActorSystem}
import net.fehmicansaglam.tepkin.TepkinMessages.ShutDown

import scala.concurrent.ExecutionContext

class MongoClient(val context: ActorRefFactory, host: String, port: Int) {
  val pool = context.actorOf(MongoPool.props(host, port).withMailbox("tepkin-mailbox"))

  def ec: ExecutionContext = context.dispatcher

  def apply(databaseName: String, collectionName: String): MongoCollection = {
    new MongoCollection(databaseName, collectionName, pool)
  }

  def collection(databaseName: String, collectionName: String): api.MongoCollection = {
    new api.MongoCollection(apply(databaseName, collectionName))
  }

  def shutdown(): Unit = {
    pool ! ShutDown
  }
}

object MongoClient {

  def create(host: String, port: Int): MongoClient = apply(host, port)

  def apply(host: String, port: Int, context: ActorRefFactory = ActorSystem("tepkin-system")): MongoClient = {
    new MongoClient(context, host, port)
  }
}
