package net.fehmicansaglam.tepkin

import akka.actor.{ActorRefFactory, ActorSystem}
import net.fehmicansaglam.tepkin.TepkinMessages.ShutDown

import scala.concurrent.ExecutionContext

class MongoClient(val context: ActorRefFactory, host: String, port: Int) {
  val pool = context.actorOf(MongoPool.props(host, port).withMailbox("tepkin-mailbox"), name = "tepkin-pool")

  implicit def ec: ExecutionContext = context.dispatcher

  def apply(databaseName: String): MongoDatabase = {
    new MongoDatabase(pool, databaseName)
  }

  def db(databaseName: String): MongoDatabase = {
    apply(databaseName)
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
