package net.fehmicansaglam.tepkin.api

import java.util.Optional
import java.util.concurrent.CompletionStage

import akka.util.Timeout
import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.protocol.message.Reply
import net.fehmicansaglam.tepkin.protocol.result.{CountResult, DeleteResult}
import net.fehmicansaglam.tepkin.{MongoCollection => ScalaCollection}

import scala.compat.java8.FutureConverters.toJava
import scala.concurrent.ExecutionContext


/**
 * Java 8 API for MongoCollection
 * @param proxy Wrapped Scala MongoCollection
 */
class MongoCollection(proxy: ScalaCollection) {

  def count(ec: ExecutionContext, timeout: Timeout): CompletionStage[CountResult] = toJava {
    proxy.count()(ec, timeout)
  }

  def count(query: BsonDocument, ec: ExecutionContext, timeout: Timeout): CompletionStage[CountResult] = toJava {
    proxy.count(query = Some(query))(ec, timeout)
  }

  def count(query: BsonDocument,
            limit: Int,
            ec: ExecutionContext,
            timeout: Timeout): CompletionStage[CountResult] = toJava {
    proxy.count(query = Some(query), limit = Some(limit))(ec, timeout)
  }

  def count(query: BsonDocument,
            limit: Int,
            skip: Int,
            ec: ExecutionContext,
            timeout: Timeout): CompletionStage[CountResult] = toJava {
    proxy.count(query = Some(query), limit = Some(limit), skip = Some(skip))(ec, timeout)
  }

  def delete(deletes: Array[BsonDocument],
             ec: ExecutionContext,
             timeout: Timeout): CompletionStage[DeleteResult] = toJava {
    proxy.delete(deletes = deletes)(ec, timeout)
  }

  def drop(ec: ExecutionContext,
           timeout: Timeout): CompletionStage[Reply] = toJava {
    proxy.drop()(ec, timeout)
  }

  def findOne(ec: ExecutionContext, timeout: Timeout): CompletionStage[Optional[BsonDocument]] = {
    findOne(BsonDocument.empty, ec, timeout)
  }

  def findOne(query: BsonDocument,
              ec: ExecutionContext,
              timeout: Timeout): CompletionStage[Optional[BsonDocument]] = toJava {
    proxy.findOne(query)(ec, timeout).map {
      case Some(document) => Optional.of[BsonDocument](document)
      case None => Optional.empty[BsonDocument]()
    }(ec)
  }
}
