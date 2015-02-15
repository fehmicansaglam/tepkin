package net.fehmicansaglam.tepkin

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.protocol.command.Count
import net.fehmicansaglam.tepkin.protocol.message.Reply

import scala.concurrent.{ExecutionContext, Future}

class MongoCollection(databaseName: String,
                      collectionName: String,
                      pool: ActorRef) {

  def count(query: Option[BsonDocument] = None)
           (implicit ec: ExecutionContext, timeout: Timeout): Future[Reply] = {
    (pool ? Count(databaseName, collectionName, query)).mapTo[Reply]
  }
}
