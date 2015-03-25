package net.fehmicansaglam.pide

import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.tepkin.MongoCollection
import net.fehmicansaglam.tepkin.protocol.WriteConcern
import net.fehmicansaglam.tepkin.protocol.result.{InsertResult, UpdateResult}

import scala.concurrent.{ExecutionContext, Future}

trait Dao[E <: Entity] {

  def collection: MongoCollection

  def insert(entity: E, writeConcern: Option[WriteConcern] = None)
            (implicit pide: Pide[E], ec: ExecutionContext, timeout: Timeout): Future[InsertResult] = {
    val document = pide.write(entity)
    writeConcern match {
      case Some(wc) => collection.insert(document, wc)
      case None => collection.insert(document)
    }
  }

  def update(entity: E, writeConcern: Option[WriteConcern] = None)
            (implicit pide: Pide[E], ec: ExecutionContext, timeout: Timeout): Future[UpdateResult] = {
    collection.update(
      query = "_id" := entity.id,
      update = pide.write(entity),
      writeConcern = writeConcern)
  }

}
