package net.fehmicansaglam.tepkin.api

import java.lang.Boolean
import java.util.concurrent.CompletableFuture
import java.util.{List => JavaList, Optional}

import akka.actor.ActorRef
import akka.stream.javadsl.Source
import akka.util.Timeout._
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.api.JavaConverters._
import net.fehmicansaglam.tepkin.protocol.command.Index
import net.fehmicansaglam.tepkin.protocol.message.Reply
import net.fehmicansaglam.tepkin.protocol.result._
import net.fehmicansaglam.tepkin.{MongoCollection => ScalaCollection}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/**
 * Java 8 API for MongoCollection
 * @param proxy Wrapped Scala MongoCollection
 */
class MongoCollection(proxy: ScalaCollection) {

  def count(ec: ExecutionContext, timeout: FiniteDuration): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count()(ec, timeout)
  }(ec)

  def count(query: BsonDocument,
            ec: ExecutionContext,
            timeout: FiniteDuration): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query))(ec, timeout)
  }(ec)

  def count(query: BsonDocument,
            limit: Integer,
            ec: ExecutionContext, timeout: FiniteDuration): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query), limit = Some(limit))(ec, timeout)
  }(ec)

  def count(query: BsonDocument,
            limit: Integer,
            skip: Integer,
            ec: ExecutionContext,
            timeout: FiniteDuration): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query), limit = Some(limit), skip = Some(skip))(ec, timeout)
  }(ec)

  /**
   * Creates indexes on this collection.
   */
  def createIndexes(indexes: JavaList[Index],
                    ec: ExecutionContext,
                    timeout: FiniteDuration): CompletableFuture[CreateIndexesResult] = toCompletableFuture {
    import scala.collection.JavaConverters._
    proxy.createIndexes(indexes.asScala: _*)(ec, timeout)
  }(ec)

  def delete(query: BsonDocument,
             ec: ExecutionContext,
             timeout: FiniteDuration): CompletableFuture[DeleteResult] = toCompletableFuture {
    proxy.delete(query)(ec, timeout)
  }(ec)

  /**
   * Finds the distinct values for a specified field across a single collection and returns the results in an array.
   *
   * @param field The field for which to return distinct values.
   */
  def distinct(field: String,
               ec: ExecutionContext,
               timeout: FiniteDuration): CompletableFuture[DistinctResult] = toCompletableFuture {
    proxy.distinct(field)(ec, timeout)
  }(ec)

  /**
   * Finds the distinct values for a specified field across a single collection and returns the results in an array.
   *
   * @param field The field for which to return distinct values.
   * @param query A query that specifies the documents from which to retrieve the distinct values.
   */
  def distinct(field: String,
               query: BsonDocument,
               ec: ExecutionContext,
               timeout: FiniteDuration): CompletableFuture[DistinctResult] = toCompletableFuture {
    proxy.distinct(field, Some(query))(ec, timeout)
  }(ec)

  /** Drops this collection */
  def drop(ec: ExecutionContext, timeout: FiniteDuration): CompletableFuture[Reply] = toCompletableFuture {
    proxy.drop()(ec, timeout)
  }(ec)

  def find(query: BsonDocument,
           ec: ExecutionContext,
           timeout: FiniteDuration): CompletableFuture[Source[JavaList[BsonDocument], ActorRef]] = toCompletableFuture {
    import scala.collection.JavaConverters._
    proxy.find(query)(ec, timeout).map(source => Source.adapt(source.map(_.asJava)))(ec)
  }(ec)

  def findAndRemove(query: BsonDocument,
                    ec: ExecutionContext,
                    timeout: FiniteDuration): CompletableFuture[Optional[BsonDocument]] = toCompletableFuture {
    proxy.findAndRemove(query = Some(query))(ec, timeout).map(toOptional)(ec)
  }(ec)

  def findOne(ec: ExecutionContext, timeout: FiniteDuration): CompletableFuture[Optional[BsonDocument]] = {
    findOne(BsonDocument.empty, ec, timeout)
  }

  def findOne(query: BsonDocument,
              ec: ExecutionContext,
              timeout: FiniteDuration): CompletableFuture[Optional[BsonDocument]] = toCompletableFuture {
    proxy.findOne(query)(ec, timeout).map(toOptional)(ec)
  }(ec)

  /**
   * Returns a list of documents that identify and describe the existing indexes on the collection.
   */
  def getIndexes(ec: ExecutionContext,
                 timeout: FiniteDuration): CompletableFuture[JavaList[Index]] = toCompletableFuture {
    import scala.collection.JavaConverters._
    proxy.getIndexes()(ec, timeout).map(_.asJava)(ec)
  }(ec)

  def insert(document: BsonDocument,
             ec: ExecutionContext,
             timeout: FiniteDuration): CompletableFuture[InsertResult] = toCompletableFuture {
    proxy.insert(Seq(document))(ec, timeout)
  }(ec)

  def insertFromSource[M](source: Source[JavaList[BsonDocument], M],
                          ec: ExecutionContext,
                          timeout: FiniteDuration): Source[InsertResult, M] = {
    import scala.collection.JavaConverters._
    Source.adapt {
      proxy.insertFromSource[M](source.asScala.map(_.asScala.toList))(ec, timeout)
    }
  }

  def insertFromSource[M](source: Source[JavaList[BsonDocument], M],
                          ordered: Boolean,
                          ec: ExecutionContext,
                          timeout: FiniteDuration): Source[InsertResult, M] = {
    import scala.collection.JavaConverters._
    Source.adapt {
      proxy.insertFromSource[M](source.asScala.map(_.asScala.toList), ordered = Some(ordered))(ec, timeout)
    }
  }

  def insertFromSource[M](source: Source[JavaList[BsonDocument], M],
                          ordered: Boolean,
                          writeConcern: BsonDocument,
                          ec: ExecutionContext,
                          timeout: FiniteDuration): Source[InsertResult, M] = {
    import scala.collection.JavaConverters._
    Source.adapt {
      proxy.insertFromSource[M](
        source.asScala.map(_.asScala.toList),
        ordered = Some(ordered),
        writeConcern = Some(writeConcern))(ec, timeout)
    }
  }

  def update(query: BsonDocument,
             update: BsonDocument,
             ec: ExecutionContext,
             timeout: FiniteDuration): CompletableFuture[UpdateResult] = toCompletableFuture {
    proxy.update(query, update)(ec, timeout)
  }(ec)

  def update(query: BsonDocument,
             update: BsonDocument,
             upsert: Boolean,
             ec: ExecutionContext,
             timeout: FiniteDuration): CompletableFuture[UpdateResult] = toCompletableFuture {
    proxy.update(query, update, upsert = Some(upsert))(ec, timeout)
  }(ec)

  def update(query: BsonDocument,
             update: BsonDocument,
             upsert: Boolean,
             multi: Boolean,
             ec: ExecutionContext,
             timeout: FiniteDuration): CompletableFuture[UpdateResult] = toCompletableFuture {
    proxy.update(query, update, upsert = Some(upsert), multi = Some(multi))(ec, timeout)
  }(ec)

  /**
   * Validates this collection. The method scans a collection’s data structures for correctness and returns
   * a single document that describes the relationship between the logical collection
   * and the physical representation of the data.
   */
  def validate(ec: ExecutionContext, timeout: FiniteDuration): CompletableFuture[BsonDocument] = toCompletableFuture {
    proxy.validate(None, None)(ec, timeout)
  }(ec)

  /**
   * Validates this collection. The method scans a collection’s data structures for correctness and returns
   * a single document that describes the relationship between the logical collection
   * and the physical representation of the data.
   *
   * @param full Specify true to enable a full validation and to return full statistics. MongoDB disables full
   *             validation by default because it is a potentially resource-intensive operation.
   */
  def validate(full: Boolean,
               ec: ExecutionContext,
               timeout: FiniteDuration): CompletableFuture[BsonDocument] = toCompletableFuture {
    proxy.validate(Some(full), None)(ec, timeout)
  }(ec)

  /**
   * Validates this collection. The method scans a collection’s data structures for correctness and returns
   * a single document that describes the relationship between the logical collection
   * and the physical representation of the data.
   *
   * @param full Specify true to enable a full validation and to return full statistics. MongoDB disables full
   *             validation by default because it is a potentially resource-intensive operation.
   * @param scandata if false skips the scan of the base collection without skipping the scan of the index.
   */
  def validate(full: Boolean,
               scandata: Boolean,
               ec: ExecutionContext,
               timeout: FiniteDuration): CompletableFuture[BsonDocument] = toCompletableFuture {
    proxy.validate(Some(full), Some(scandata))(ec, timeout)
  }(ec)
}
