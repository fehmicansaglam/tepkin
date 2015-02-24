package net.fehmicansaglam.tepkin

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._
import net.fehmicansaglam.tepkin.protocol.command._
import net.fehmicansaglam.tepkin.protocol.message.{QueryMessage, Reply}
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
        document.getAs[Boolean]("missing"),
        document.getAs[Double]("n").get.toLong,
        document.getAs[Double]("ok").get == 1.0
      )
    }
  }

  def delete(deletes: Seq[BsonDocument], ordered: Boolean = true, writeConcern: Option[BsonDocument] = None)
            (implicit ec: ExecutionContext, timeout: Timeout): Future[DeleteResult] = {
    (pool ? Delete(databaseName, collectionName, deletes.map(doc => ("q" := doc) ~ ("limit" := 1)), ordered, writeConcern)).mapTo[Reply].map { reply =>
      val document = reply.documents(0)
      DeleteResult(
        document.getAs[Int]("n"),
        document.getAs[Int]("code"),
        document.getAs[String]("errmsg"),
        document.getAs[Int]("ok").get == 1
      )
    }
  }

  def drop()(implicit ec: ExecutionContext, timeout: Timeout): Future[Reply] = {
    (pool ? Drop(databaseName, collectionName)).mapTo[Reply]
  }

  def find(query: BsonDocument)(implicit ec: ExecutionContext, timeout: Timeout): Future[Source[List[BsonDocument]]] = {
    (pool ? QueryMessage(s"$databaseName.$collectionName", query)).mapTo[Reply].map { reply =>
      if (reply.cursorID == 0) {
        Source.single(reply.documents)
      } else {
        Source.single(reply.documents) ++
          Source(MongoCursor.props(pool, s"$databaseName.$collectionName", reply.cursorID))
      }
    }
  }

  def findAndModify(query: Option[BsonDocument] = None,
                    sort: Option[BsonDocument] = None,
                    removeOrUpdate: Either[Boolean, BsonDocument],
                    returnNew: Boolean = false,
                    fields: Option[Seq[String]] = None,
                    upsert: Boolean = false)
                   (implicit ec: ExecutionContext, timeout: Timeout): Future[Option[BsonDocument]] = {
    (pool ? FindAndModify(databaseName, collectionName, query, sort, removeOrUpdate, returnNew, fields, upsert))
      .mapTo[Reply]
      .map(_.documents.headOption.flatMap(_.getAs("value")))
  }

  def findOne(query: BsonDocument)(implicit ec: ExecutionContext, timeout: Timeout): Future[Option[BsonDocument]] = {
    (pool ? QueryMessage(s"$databaseName.$collectionName", query, numberToReturn = 1))
      .mapTo[Reply]
      .map(_.documents.headOption)
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
