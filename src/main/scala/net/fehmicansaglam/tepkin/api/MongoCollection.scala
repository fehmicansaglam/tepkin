package net.fehmicansaglam.tepkin.api

import java.util.concurrent.CompletableFuture
import java.util.{List => JavaList, Optional}

import akka.stream.javadsl.Source
import akka.util.Timeout
import net.fehmicansaglam.tepkin.api.JavaConverters._
import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.protocol.message.Reply
import net.fehmicansaglam.tepkin.protocol.result.{CountResult, DeleteResult, InsertResult}
import net.fehmicansaglam.tepkin.{MongoCollection => ScalaCollection}

import scala.concurrent.ExecutionContext


/**
 * Java 8 API for MongoCollection
 * @param proxy Wrapped Scala MongoCollection
 */
class MongoCollection(proxy: ScalaCollection) {

  def count(implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count()
  }

  def count(query: BsonDocument)
           (implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query))
  }

  def count(query: BsonDocument, limit: Int)
           (implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query), limit = Some(limit))
  }

  def count(query: BsonDocument, limit: Int, skip: Int)
           (implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query), limit = Some(limit), skip = Some(skip))
  }

  def count(query: BsonDocument, limit: Optional[Int], skip: Optional[Int])
           (implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query), limit = limit, skip = skip)
  }

  def delete(deletes: Array[BsonDocument])
            (implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[DeleteResult] = toCompletableFuture {
    proxy.delete(deletes = deletes)
  }

  /** Drops this collection */
  def drop(implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[Reply] = toCompletableFuture {
    proxy.drop()
  }

  def find(query: BsonDocument)
          (implicit ec: ExecutionContext,
           timeout: Timeout): CompletableFuture[Source[JavaList[BsonDocument]]] = toCompletableFuture {
    import scala.collection.JavaConverters._
    proxy.find(query).map(source => Source.adapt(source.map(_.asJava)))
  }

  def findAndRemove(query: BsonDocument)
                   (implicit ec: ExecutionContext,
                    timeout: Timeout): CompletableFuture[Optional[BsonDocument]] = toCompletableFuture {
    proxy.findAndRemove(query = Some(query)).map(toOptional)
  }

  def findOne(implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[Optional[BsonDocument]] = {
    findOne(BsonDocument.empty)
  }

  def findOne(query: BsonDocument)
             (implicit ec: ExecutionContext,
              timeout: Timeout): CompletableFuture[Optional[BsonDocument]] = toCompletableFuture {
    proxy.findOne(query).map(toOptional)
  }

  def insert(document: BsonDocument)
            (implicit ec: ExecutionContext, timeout: Timeout): CompletableFuture[InsertResult] = toCompletableFuture {
    proxy.insert(Seq(document))
  }
}
