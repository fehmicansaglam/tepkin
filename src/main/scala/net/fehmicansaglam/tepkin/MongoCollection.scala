package net.fehmicansaglam.tepkin

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.protocol.command.{Count, Delete, Insert}
import net.fehmicansaglam.tepkin.protocol.message.Reply
import net.fehmicansaglam.tepkin.protocol.result.{CountResult, DeleteResult, InsertResult}

import scala.concurrent.{ExecutionContext, Future}

class MongoCollection(databaseName: String,
                      collectionName: String,
                      pool: ActorRef) {

  def count(query: Option[BsonDocument] = None,
            limit: Option[Int] = None,
            skip: Option[Int] = None)
           (implicit ec: ExecutionContext, timeout: Timeout): Future[CountResult] = {

    (pool ? Count(databaseName, collectionName, query, limit, skip)).mapTo[Reply].map { reply =>
      val document = reply.documents(0)
      CountResult(
        document.getAs("missing"),
        document.getAs[Double]("n").get.toLong,
        document.getAs[Double]("ok").get == 1.0
      )
    }
  }

  def delete(deletes: Seq[BsonDocument], ordered: Boolean = true, writeConcern: Option[BsonDocument] = None)
            (implicit ec: ExecutionContext, timeout: Timeout): Future[DeleteResult] = {
    (pool ? Delete(databaseName, collectionName, deletes, ordered, writeConcern)).mapTo[Reply].map { reply =>
      val document = reply.documents(0)
      DeleteResult(
        document.getAs[Int]("n"),
        document.getAs[Int]("code"),
        document.getAs[String]("errmsg"),
        document.getAs[Int]("ok").get == 1
      )
    }
  }

  def insert(documents: Seq[BsonDocument], ordered: Boolean = true, writeConcern: Option[BsonDocument] = None)
            (implicit ec: ExecutionContext, timeout: Timeout): Future[InsertResult] = {
    (pool ? Insert(databaseName, collectionName, documents, ordered, writeConcern)).mapTo[Reply].map { reply =>
      val document = reply.documents(0)
      InsertResult(
        document.getAs[Int]("n").get,
        document.getAs[Int]("ok").get == 1
      )
    }
  }
}
