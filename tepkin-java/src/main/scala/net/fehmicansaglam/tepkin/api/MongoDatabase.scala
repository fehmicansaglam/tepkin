package net.fehmicansaglam.tepkin.api

import java.util.concurrent.CompletableFuture
import java.util.{List => JavaList}

import akka.actor.ActorRef
import akka.stream.javadsl.Source
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin
import net.fehmicansaglam.tepkin.api.JavaConverters._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class MongoDatabase(proxy: tepkin.MongoDatabase) {

  def collection(collectionName: String): MongoCollection = {
    new MongoCollection(proxy.apply(collectionName))
  }

  def gridFs(): GridFs = {
    new GridFs(proxy.gridFs())
  }

  def gridFs(prefix: String): GridFs = {
    new GridFs(proxy.gridFs(prefix))
  }

  def listCollections(ec: ExecutionContext,
                      timeout: FiniteDuration): CompletableFuture[Source[JavaList[BsonDocument], ActorRef]] = {
    toCompletableFuture {
      import scala.collection.JavaConverters._
      proxy.listCollections()(ec, timeout).map(source => Source.adapt(source.map(_.asJava)))(ec)
    }(ec)
  }

  def listCollections(filter: BsonDocument,
                      ec: ExecutionContext,
                      timeout: FiniteDuration): CompletableFuture[Source[JavaList[BsonDocument], ActorRef]] = {
    toCompletableFuture {
      import scala.collection.JavaConverters._
      proxy.listCollections(Some(filter))(ec, timeout).map(source => Source.adapt(source.map(_.asJava)))(ec)
    }(ec)
  }
}

