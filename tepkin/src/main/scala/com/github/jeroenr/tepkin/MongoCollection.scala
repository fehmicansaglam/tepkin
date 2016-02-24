package com.github.jeroenr.tepkin

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.{Sink, Source}
import akka.util.Timeout
import com.github.jeroenr.bson.{BsonDocument, BsonValueNumber, Bulk}
import com.github.jeroenr.tepkin.protocol.WriteConcern
import com.github.jeroenr.tepkin.protocol.command._
import com.github.jeroenr.tepkin.protocol.message.{QueryMessage, QueryOptions, Reply}
import com.github.jeroenr.tepkin.protocol.result._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class MongoCollection(databaseName: String,
                      collectionName: String,
                      pool: ActorRef) {

  /**
   * Calculates aggregate values for the data in this collection.
   *
   * @param pipeline A sequence of data aggregation operations or stages.
   * @param explain Specifies to return the information on the processing of the pipeline.
   * @param allowDiskUse Enables writing to temporary files. When set to true, aggregation operations can write data
   *                     to the _tmp subdirectory in the dbPath directory.
   * @param cursor Specifies the initial batch size for the cursor. The value of the cursor field is a document with
   *               the field batchSize.
   */
  def aggregate(pipeline: List[BsonDocument],
                explain: Option[Boolean] = None,
                allowDiskUse: Option[Boolean] = None,
                cursor: Option[BsonDocument] = None,
                batchMultiplier: Int = 1000)
               (implicit timeout: Timeout): Source[List[BsonDocument], ActorRef] = {
    val source: Source[List[BsonDocument], ActorRef] = Source.actorPublisher(MongoCursor.props(
      pool,
      Aggregate(databaseName, collectionName, pipeline, explain, allowDiskUse, cursor),
      reply => (s"$databaseName.$collectionName", reply.cursorID, reply.documents),
      batchMultiplier,
      timeout))

    source.mapConcat(_.flatMap(_.getAsList[BsonDocument]("result")))
  }

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
      val document = reply.documents.head
      CountResult(
        document.getAs[Boolean]("missing"),
        document.get[BsonValueNumber]("n").map(_.toInt).getOrElse(0),
        document.get[BsonValueNumber]("ok").map(_.toInt).getOrElse(0) == 1
      )
    }
  }

  /**
   * Creates indexes on this collection.
   */
  def createIndexes(indexes: Index*)
                   (implicit ec: ExecutionContext, timeout: Timeout): Future[CreateIndexesResult] = {
    (pool ? CreateIndexes(databaseName, collectionName, indexes: _*)).mapTo[Reply].map { reply =>
      val document = reply.documents.head
      CreateIndexesResult(document).convertErrorToException()
    }
  }

  /**
   * Removes documents from a collection.
   *
   * @param query Specifies deletion criteria using query operators.
   *              To delete all documents in a collection, pass an empty document ({}).
   * @param justOne To limit the deletion to just one document, set to true.
   *                Omit to use the default value of false and delete all documents matching the deletion criteria.
   * @param writeConcern A document expressing the write concern. Omit to use the default write concern.
   * @return A WriteResult object that contains the status of the operation.
   */
  def delete(query: BsonDocument, justOne: Boolean = false, writeConcern: Option[WriteConcern] = None)
            (implicit ec: ExecutionContext, timeout: Timeout): Future[DeleteResult] = {
    (pool ? Delete(
      databaseName,
      collectionName,
      deletes = Seq(DeleteElement(query, justOne match {
        case false => 0
        case true => 1
      })),
      writeConcern = writeConcern.map(_.toDoc))).mapTo[Reply].map { reply =>
      val document = reply.documents.head
      DeleteResult(
        document.get[BsonValueNumber]("ok").map(_.toInt).getOrElse(0) == 1,
        document.getAs[Int]("n").getOrElse(0),
        writeErrors = document.getAsList[BsonDocument]("writeErrors").map(_.map(WriteError(_))),
        writeConcernError = document.getAs[BsonDocument]("writeConcernError").map(WriteConcernError(_))
      ).convertErrorToException()
    }
  }

  /**
   * Finds the distinct values for a specified field across a single collection and returns the results in an array.
   * @param field The field for which to return distinct values.
   * @param query A query that specifies the documents from which to retrieve the distinct values.
   */
  def distinct(field: String, query: Option[BsonDocument] = None)
              (implicit ec: ExecutionContext, timeout: Timeout): Future[DistinctResult] = {
    (pool ? Distinct(databaseName, collectionName, field, query)).mapTo[Reply].map { reply =>
      val document = reply.documents.head
      DistinctResult(document)
    }
  }

  /** Drops this collection */
  def drop()(implicit ec: ExecutionContext, timeout: Timeout): Future[Reply] = {
    (pool ? Drop(databaseName, collectionName)).mapTo[Reply]
  }

  /**
   * Selects documents in this collection.
   *
   * @param query Specifies selection criteria using query operators. To return all documents in a collection,
   *              pass an empty document ({}).
   * @param fields Specifies the fields to return using projection operators. To return all fields in the matching
   *               document, omit this parameter.
   */
  def find(query: BsonDocument,
           fields: Option[BsonDocument] = None,
           skip: Int = 0,
           tailable: Boolean = false,
           batchMultiplier: Int = 1000)
          (implicit timeout: Timeout): Source[List[BsonDocument], ActorRef] = {
    val flags = if (tailable) QueryOptions.TailableCursor | QueryOptions.AwaitData else 0
    Source.actorPublisher(MongoCursor.props(
      pool,
      QueryMessage(s"$databaseName.$collectionName", query, fields = fields, flags = flags, numberToSkip = skip),
      reply => (s"$databaseName.$collectionName", reply.cursorID, reply.documents),
      batchMultiplier,
      timeout))
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
  def findOne(query: BsonDocument = BsonDocument.empty, skip: Int = 0)
             (implicit ec: ExecutionContext, timeout: Timeout): Future[Option[BsonDocument]] = {
    (pool ? QueryMessage(s"$databaseName.$collectionName", query, numberToSkip = skip, numberToReturn = 1))
      .mapTo[Reply]
      .map(_.documents.headOption)
  }

  /** Retrieves a random document matching the given selector. */
  def findRandom(query: Option[BsonDocument] = None)
                (implicit ec: ExecutionContext, timeout: Timeout): Future[Option[BsonDocument]] = {
    for {
      count <- count(query)
      index = if (count.n == 0) 0 else Random.nextInt(count.n)
      random <- findOne(query.getOrElse(BsonDocument.empty), skip = index)
    } yield random
  }

  /**
   * Inserts document into this collection.
   *
   * @param document document to insert into the collection.
   */
  def insert(document: BsonDocument)(implicit ec: ExecutionContext, timeout: Timeout): Future[InsertResult] = {
    insert(Seq(document))
  }

  /**
   * Inserts document into this collection.
   *
   * @param document document to insert into the collection.
   */
  def insert(document: BsonDocument, writeConcern: WriteConcern)
            (implicit ec: ExecutionContext, timeout: Timeout): Future[InsertResult] = {
    insert(Seq(document), writeConcern = Some(writeConcern))
  }

  /**
   * Inserts documents into a collection.
   *
   * @param documents A sequence of documents to insert into the collection.
   * @param ordered If true, perform an ordered insert of the documents in the array, and if an error occurs
   *                with one of documents, MongoDB will return without processing the remaining documents in the array.
   *                If false, perform an unordered insert, and if an error occurs with one of documents,
   *                continue processing the remaining documents in the array.
   * @param writeConcern A document expressing the write concern.
   */
  def insert(documents: Seq[BsonDocument], ordered: Option[Boolean] = None, writeConcern: Option[WriteConcern] = None)
            (implicit ec: ExecutionContext, timeout: Timeout): Future[InsertResult] = {
    (pool ? Insert(databaseName, collectionName, documents, ordered, writeConcern.map(_.toDoc)))
      .mapTo[Reply].map { reply =>
      val document = reply.documents.head
      InsertResult(
        document.get[BsonValueNumber]("ok").map(_.toInt).getOrElse(0) == 1,
        document.getAs[Int]("n").getOrElse(0),
        writeErrors = document.getAsList[BsonDocument]("writeErrors").map(_.map(WriteError(_))),
        writeConcernError = document.getAs[BsonDocument]("writeConcernError").map(WriteConcernError(_))
      ).convertErrorToException()
    }
  }

  /**
   * Inserts documents into a collection.
   *
   * @param source A source of documents to insert into the collection.
   * @param ordered If true, perform an ordered insert of the documents in the array, and if an error occurs
   *                with one of documents, MongoDB will return without processing the remaining documents in the array.
   *                If false, perform an unordered insert, and if an error occurs with one of documents,
   *                continue processing the remaining documents in the array.
   * @param writeConcern A document expressing the write concern.
   */
  def insertFromSource[M](source: Source[List[BsonDocument], M],
                          ordered: Option[Boolean] = None,
                          writeConcern: Option[WriteConcern] = None)
                         (implicit ec: ExecutionContext, timeout: Timeout): Source[InsertResult, M] = {
    source.mapAsync(1)(documents => insert(documents, ordered, writeConcern))
  }


  /**
   * Returns a list of documents that identify and describe the existing indexes on this collection.
   */
  def getIndexes()(implicit ec: ExecutionContext, timeout: Timeout): Future[List[Index]] = {
    (pool ? ListIndexes(databaseName, collectionName)).mapTo[Reply].map { reply =>
      reply.documents.head.getAs[BsonDocument]("cursor")
        .flatMap(_.getAsList[BsonDocument]("firstBatch"))
        .getOrElse(Nil)
        .map(Index.apply)
    }
  }

  /**
   * Returns a sink which inserts bulk documents.
   *
   * @param parallelism max number of parallel insert commands.
   */
  def sink(parallelism: Int = 1,
           ordered: Option[Boolean] = None,
           writeConcern: Option[WriteConcern] = None): Sink[Bulk, ActorRef] = {
    Sink.actorSubscriber(InsertSink.props(databaseName, collectionName, pool, parallelism, ordered, writeConcern))
  }

  /**
   * Modifies an existing document or documents in a collection. The method can modify specific fields of an existing
   * document or documents or replace an existing document entirely, depending on the update parameter.
   *
   * @param query The selection criteria for the update. Use the same query selectors as used in the find() method.
   * @param update The modifications to apply.
   * @param upsert If set to true, creates a new document when no document matches the query criteria.
   *               The default value is false, which does not insert a new document when no match is found.
   * @param multi  If set to true, updates multiple documents that meet the query criteria.
   *               If set to false, updates one document.  The default value is false.
   * @param writeConcern A document expressing the write concern.
   */
  def update(query: BsonDocument,
             update: BsonDocument,
             upsert: Option[Boolean] = None,
             multi: Option[Boolean] = None,
             writeConcern: Option[WriteConcern] = None)
            (implicit ec: ExecutionContext, timeout: Timeout): Future[UpdateResult] = {
    (pool ? Update(
      databaseName,
      collectionName,
      updates = Seq(UpdateElement(q = query, u = update, upsert = upsert, multi = multi)),
      writeConcern = writeConcern.map(_.toDoc)
    )).mapTo[Reply].map { reply =>
      val document = reply.documents.head
      UpdateResult(
        ok = document.get[BsonValueNumber]("ok").map(_.toInt).getOrElse(0) == 1,
        n = document.get[BsonValueNumber]("n").map(_.toInt).getOrElse(0),
        nModified = document.get[BsonValueNumber]("nModified").map(_.toInt).getOrElse(0),
        upserted = document.getAsList[BsonDocument]("upserted"),
        writeErrors = document.getAsList[BsonDocument]("writeErrors").map(_.map(WriteError(_))),
        writeConcernError = document.getAs[BsonDocument]("writeConcernError").map(WriteConcernError(_))
      ).convertErrorToException()
    }
  }

  /**
   * Validates a collection. The method scans a collection’s data structures for correctness
   * and returns a single document that describes the relationship between the logical collection
   * and the physical representation of the data.
   * @param full Specify true to enable a full validation and to return full statistics.
   *             MongoDB disables full validation by default because it is a potentially resource-intensive operation.
   * @param scandata if false skips the scan of the base collection without skipping the scan of the index.
   */
  def validate(full: Option[Boolean] = None, scandata: Option[Boolean] = None)
              (implicit ec: ExecutionContext, timeout: Timeout): Future[BsonDocument] = {
    (pool ? Validate(databaseName, collectionName)).mapTo[Reply].map(_.documents.head)
  }
}
