package com.github.jeroenr.tepkin

import akka.actor.ActorDSL._
import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Terminated}
import com.github.jeroenr.tepkin.TepkinMessage.ShutDown
import com.github.jeroenr.tepkin.protocol.ReadPreference

import scala.concurrent.ExecutionContext

class MongoClient(_context: ActorRefFactory, uri: MongoClientUri, nConnectionsPerNode: Int) {
  val poolManager = _context.actorOf(
    MongoPoolManager
      .props(uri, nConnectionsPerNode, uri.option("readPreference").map(ReadPreference.apply))
      .withMailbox("tepkin-mailbox"),
    name = "tepkin-pool")


  implicit def context: ActorRefFactory = _context

  implicit def ec: ExecutionContext = _context.dispatcher

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

  /** Shutdown after all specified actors are terminated */
  def shutdown(ref: ActorRef, refs: ActorRef*): Unit = {
    val allRefs = refs :+ ref

    actor(new Act {
      var remaining = allRefs.length

      whenStarting {
        allRefs.foreach(context.watch)
      }

      become {
        case _: Terminated =>
          remaining -= 1
          if (remaining == 0) {
            poolManager ! ShutDown
            context.stop(self)
          }
      }
    })

    ()
  }
}

object MongoClient {

  def apply(uri: String,
            nConnectionsPerNode: Int = 10,
            context: ActorRefFactory = ActorSystem("tepkin-system")): MongoClient = {
    new MongoClient(context, MongoClientUri(uri), nConnectionsPerNode)
  }

}
