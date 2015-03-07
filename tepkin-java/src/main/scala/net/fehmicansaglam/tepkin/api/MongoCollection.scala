package net.fehmicansaglam.tepkin.api

import java.lang.Boolean
import java.util.concurrent.CompletableFuture
import java.util.{List => JavaList, Optional}

import akka.actor.ActorRef
import akka.stream.javadsl.Source
import akka.util.Timeout._
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.api.JavaConverters._
import net.fehmicansaglam.tepkin.protocol.message.Reply
import net.fehmicansaglam.tepkin.protocol.result.{CountResult, DeleteResult, InsertResult, UpdateResult}
import net.fehmicansaglam.tepkin.{MongoCollection => ScalaCollection}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration


/**
 * Java 8 API for MongoCollection
 * @param proxy Wrapped Scala MongoCollection
 */
class MongoCollection(proxy: ScalaCollection) {

  def count(ec: ExecutionContext, duration: FiniteDuration): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count()(ec, duration)
  }(ec)

  def count(query: BsonDocument,
            ec: ExecutionContext,
            duration: FiniteDuration): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query))(ec, duration)
  }(ec)

  def count(query: BsonDocument,
            limit: Integer,
            ec: ExecutionContext, duration: FiniteDuration): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query), limit = Some(limit))(ec, duration)
  }(ec)

  def count(query: BsonDocument,
            limit: Integer,
            skip: Integer,
            ec: ExecutionContext,
            duration: FiniteDuration): CompletableFuture[CountResult] = toCompletableFuture {
    proxy.count(query = Some(query), limit = Some(limit), skip = Some(skip))(ec, duration)
  }(ec)

  def delete(query: BsonDocument,
             ec: ExecutionContext,
             duration: FiniteDuration): CompletableFuture[DeleteResult] = toCompletableFuture {
    proxy.delete(query)(ec, duration)
  }(ec)

  /** Drops this collection */
  def drop(ec: ExecutionContext, duration: FiniteDuration): CompletableFuture[Reply] = toCompletableFuture {
    proxy.drop()(ec, duration)
  }(ec)

  def find(query: BsonDocument,
           ec: ExecutionContext,
           duration: FiniteDuration): CompletableFuture[Source[JavaList[BsonDocument], ActorRef]] = toCompletableFuture {
    import scala.collection.JavaConverters._
    proxy.find(query)(ec, duration).map(source => Source.adapt(source.map(_.asJava)))(ec)
  }(ec)

  def findAndRemove(query: BsonDocument,
                    ec: ExecutionContext,
                    duration: FiniteDuration): CompletableFuture[Optional[BsonDocument]] = toCompletableFuture {
    proxy.findAndRemove(query = Some(query))(ec, duration).map(toOptional)(ec)
  }(ec)

  def findOne(ec: ExecutionContext, duration: FiniteDuration): CompletableFuture[Optional[BsonDocument]] = {
    findOne(BsonDocument.empty, ec, duration)
  }

  def findOne(query: BsonDocument,
              ec: ExecutionContext,
              duration: FiniteDuration): CompletableFuture[Optional[BsonDocument]] = toCompletableFuture {
    proxy.findOne(query)(ec, duration).map(toOptional)(ec)
  }(ec)

  def insert(document: BsonDocument,
             ec: ExecutionContext,
             duration: FiniteDuration): CompletableFuture[InsertResult] = toCompletableFuture {
    proxy.insert(Seq(document))(ec, duration)
  }(ec)

  def update(query: BsonDocument,
             update: BsonDocument,
             ec: ExecutionContext,
             duration: FiniteDuration): CompletableFuture[UpdateResult] = toCompletableFuture {
    proxy.update(query, update)(ec, duration)
  }(ec)

  def update(query: BsonDocument,
             update: BsonDocument,
             upsert: Boolean,
             ec: ExecutionContext,
             duration: FiniteDuration): CompletableFuture[UpdateResult] = toCompletableFuture {
    proxy.update(query, update, upsert = Some(upsert))(ec, duration)
  }(ec)

  def update(query: BsonDocument,
             update: BsonDocument,
             upsert: Boolean,
             multi: Boolean,
             ec: ExecutionContext,
             duration: FiniteDuration): CompletableFuture[UpdateResult] = toCompletableFuture {
    proxy.update(query, update, upsert = Some(upsert), multi = Some(multi))(ec, duration)
  }(ec)

}
