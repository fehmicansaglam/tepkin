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

  /**
   * Counts the number of documents in this collection.
   *
   * @param query A query that selects which documents to count in a collection.
   * @param limit The maximum number of matching documents to return.
   * @param skip The number of matching documents to skip before returning results.
   */
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

  /** Drops this collection */
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

  /**
   * Updates and returns a single document. It returns the old document by default.
   *
   * @param query The selection criteria for the update.
   * @param sort Determines which model the operation updates if the query selects multiple models.
   *             findAndUpdate() updates the first model in the sort order specified by this argument.
   * @param update Performs an update of the selected model.
   * @param returnNew When true, returns the updated model rather than the original.
   * @param fields A subset of fields to return.
   * @param upsert When true, findAndUpdate() creates a new model if no model matches the query.
   */
  def findAndUpdate(query: Option[BsonDocument] = None,
                    sort: Option[BsonDocument] = None,
                    update: BsonDocument,
                    returnNew: Boolean = false,
                    fields: Option[Seq[String]] = None,
                    upsert: Boolean = false)
                   (implicit ec: ExecutionContext, timeout: Timeout): Future[Option[BsonDocument]] = {
    (pool ? FindAndModify(databaseName, collectionName, query, sort, Right(update), returnNew, fields, upsert))
      .mapTo[Reply]
      .map(_.documents.headOption.flatMap(_.getAs("value")))
  }

  /**
   * Removes and returns a single document.
   *
   * @param query The selection criteria for the remove.
   * @param sort Determines which model the operation removes if the query selects multiple models.
   *             findAndRemove() removes the first model in the sort order specified by this argument.
   * @param fields A subset of fields to return.
   */
  def findAndRemove(query: Option[BsonDocument] = None,
                    sort: Option[BsonDocument] = None,
                    fields: Option[Seq[String]] = None)
                   (implicit ec: ExecutionContext, timeout: Timeout): Future[Option[BsonDocument]] = {
    (pool ? FindAndModify(databaseName, collectionName, query, sort, Left(true), fields = fields))
      .mapTo[Reply]
      .map(_.documents.headOption.flatMap(_.getAs("value")))
  }

  /** Retrieves at most one document matching the given selector. */
  def findOne(query: BsonDocument)(implicit ec: ExecutionContext, timeout: Timeout): Future[Option[BsonDocument]] = {
    (pool ? QueryMessage(s"$databaseName.$collectionName", query, numberToReturn = 1))
      .mapTo[Reply]
      .map(_.documents.headOption)
  }

  /**
   * Inserts a document or documents into a collection.
   *
   * @param documents A sequence of documents to insert into the collection.
   * @param ordered If true, perform an ordered insert of the documents in the array, and if an error occurs
   *                with one of documents, MongoDB will return without processing the remaining documents in the array.
   *                If false, perform an unordered insert, and if an error occurs with one of documents,
   *                continue processing the remaining documents in the array.
   * @param writeConcern A document expressing the write concern.
   */
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
