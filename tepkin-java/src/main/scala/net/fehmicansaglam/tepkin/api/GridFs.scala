package net.fehmicansaglam.tepkin.api

import java.io.File
import java.util.Optional
import java.util.concurrent.CompletableFuture

import akka.actor.ActorRef
import akka.stream.FlowMaterializer
import akka.stream.javadsl.Source
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.Implicits.BsonValueObjectId
import net.fehmicansaglam.tepkin
import net.fehmicansaglam.tepkin.GridFs.Chunk
import net.fehmicansaglam.tepkin.api.JavaConverters._
import net.fehmicansaglam.tepkin.protocol.result.DeleteResult

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class GridFs(proxy: tepkin.GridFs) {

  def findOne(query: BsonDocument,
              ec: ExecutionContext,
              timeout: FiniteDuration): CompletableFuture[Optional[BsonDocument]] = toCompletableFuture {
    proxy.findOne(query)(ec, timeout).map(toOptional)(ec)
  }(ec)


  def put(file: File,
          mat: FlowMaterializer,
          ec: ExecutionContext,
          timeout: FiniteDuration): CompletableFuture[BsonDocument] = toCompletableFuture {
    proxy.put(file)(mat, ec, timeout)
  }(ec)

  /**
   * Gets the file as a Chunk stream.
   *
   * @param id _id of the file
   */
  def get(id: BsonValueObjectId,
          ec: ExecutionContext,
          timeout: FiniteDuration): CompletableFuture[Source[Chunk, ActorRef]] = toCompletableFuture {
    proxy.get(id)(ec, timeout).map(Source.adapt)(ec)
  }(ec)

  def getOne(query: BsonDocument,
             ec: ExecutionContext,
             timeout: FiniteDuration): CompletableFuture[Optional[Source[Chunk, ActorRef]]] = toCompletableFuture {
    proxy.getOne(query)(ec, timeout).map(option => toOptional(option.map(Source.adapt)))(ec)
  }(ec)

  /**
   * Deletes the specified file from GridFS storage.
   *
   * @param id _id of the file
   */
  def delete(id: BsonValueObjectId,
             ec: ExecutionContext,
             timeout: FiniteDuration): CompletableFuture[DeleteResult] = toCompletableFuture {
    proxy.delete(id)(ec, timeout)
  }(ec)

  /** Deletes at most one file from GridFS storage matching the given criteria. */
  def deleteOne(query: BsonDocument,
                ec: ExecutionContext,
                timeout: FiniteDuration): CompletableFuture[DeleteResult] = toCompletableFuture {
    proxy.deleteOne(query)(ec, timeout)
  }(ec)
}
