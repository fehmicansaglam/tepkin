package net.fehmicansaglam.tepkin.api

import java.util.concurrent.CompletableFuture
import java.util.{List => JavaList, Optional}

import akka.stream.javadsl.Source
import akka.util.Timeout
import net.fehmicansaglam.tepkin.api.FutureConverters.toJava
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

  def count(ec: ExecutionContext, timeout: Timeout): CompletableFuture[CountResult] = toJava {
    proxy.count()(ec, timeout)
  }(ec)

  def count(query: BsonDocument, ec: ExecutionContext, timeout: Timeout): CompletableFuture[CountResult] = toJava {
    proxy.count(query = Some(query))(ec, timeout)
  }(ec)

  def count(query: BsonDocument,
            limit: Int,
            ec: ExecutionContext,
            timeout: Timeout): CompletableFuture[CountResult] = toJava {
    proxy.count(query = Some(query), limit = Some(limit))(ec, timeout)
  }(ec)

  def count(query: BsonDocument,
            limit: Int,
            skip: Int,
            ec: ExecutionContext,
            timeout: Timeout): CompletableFuture[CountResult] = toJava {
    proxy.count(query = Some(query), limit = Some(limit), skip = Some(skip))(ec, timeout)
  }(ec)

  def delete(deletes: Array[BsonDocument],
             ec: ExecutionContext,
             timeout: Timeout): CompletableFuture[DeleteResult] = toJava {
    proxy.delete(deletes = deletes)(ec, timeout)
  }(ec)

  def drop(ec: ExecutionContext, timeout: Timeout): CompletableFuture[Reply] = toJava {
    proxy.drop()(ec, timeout)
  }(ec)

  def find(query: BsonDocument,
           ec: ExecutionContext,
           timeout: Timeout): CompletableFuture[Source[JavaList[BsonDocument]]] = toJava {
    import scala.collection.JavaConverters._
    proxy.find(query)(ec, timeout).map(source => Source.adapt(source.map(_.asJava)))(ec)
  }(ec)

  def findOne(ec: ExecutionContext, timeout: Timeout): CompletableFuture[Optional[BsonDocument]] = {
    findOne(BsonDocument.empty, ec, timeout)
  }

  def findOne(query: BsonDocument,
              ec: ExecutionContext,
              timeout: Timeout): CompletableFuture[Optional[BsonDocument]] = toJava {
    proxy.findOne(query)(ec, timeout).map {
      case Some(document) => Optional.of[BsonDocument](document)
      case None => Optional.empty[BsonDocument]()
    }(ec)
  }(ec)

  def insert(document: BsonDocument, ec: ExecutionContext, timeout: Timeout): CompletableFuture[InsertResult] = toJava {
    proxy.insert(Seq(document))(ec, timeout)
  }(ec)
}
