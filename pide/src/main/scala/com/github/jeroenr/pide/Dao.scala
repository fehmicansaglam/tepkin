package com.github.jeroenr.pide

import akka.actor.ActorRef
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}
import com.github.jeroenr.tepkin.MongoCollection
import com.github.jeroenr.tepkin.protocol.WriteConcern
import com.github.jeroenr.tepkin.protocol.result.{InsertResult, UpdateResult}

import scala.concurrent.{ExecutionContext, Future}

trait Dao[ID, E <: Entity[ID]] {

  def collection: MongoCollection

  def find(query: BsonDocument,
           fields: Option[BsonDocument] = None,
           skip: Int = 0,
           tailable: Boolean = false,
           batchMultiplier: Int = 1000)
          (implicit pide: Pide[ID, E], timeout: Timeout): Source[List[E], ActorRef] = {
    collection.find(query, fields, skip, tailable, batchMultiplier).map(_.map(pide.read))
  }

  def findAndRemove(query: Option[BsonDocument] = None,
                    sort: Option[BsonDocument] = None,
                    fields: Option[Seq[String]] = None)
                   (implicit pide: Pide[ID, E], ec: ExecutionContext, timeout: Timeout): Future[Option[E]] = {
    collection.findAndRemove(query, sort, fields).map(_.map(pide.read))
  }

  def findAndUpdate(query: Option[BsonDocument] = None,
                    sort: Option[BsonDocument] = None,
                    update: BsonDocument,
                    returnNew: Boolean = false,
                    fields: Option[Seq[String]] = None,
                    upsert: Boolean = false)
                   (implicit pide: Pide[ID, E], ec: ExecutionContext, timeout: Timeout): Future[Option[E]] = {
    collection.findAndUpdate(query, sort, update, returnNew, fields, upsert).map(_.map(pide.read))
  }

  def findOne(query: BsonDocument = BsonDocument.empty, skip: Int = 0)
             (implicit pide: Pide[ID, E], ec: ExecutionContext, timeout: Timeout): Future[Option[E]] = {
    collection.findOne(query, skip).map(_.map(pide.read))
  }

  def findRandom(query: Option[BsonDocument] = None)
                (implicit pide: Pide[_, E], ec: ExecutionContext, timeout: Timeout): Future[Option[E]] = {
    collection.findRandom(query).map(_.map(pide.read))
  }

  def insert(entity: E, writeConcern: Option[WriteConcern] = None)
            (implicit pide: Pide[ID, E], ec: ExecutionContext, timeout: Timeout): Future[InsertResult] = {
    val document = pide.write(entity)
    writeConcern match {
      case Some(wc) => collection.insert(document, wc)
      case None => collection.insert(document)
    }
  }

  def update(entity: E, writeConcern: Option[WriteConcern] = None)
            (implicit pide: Pide[ID, E], ec: ExecutionContext, timeout: Timeout): Future[UpdateResult] = {
    collection.update(
      query = "_id" := pide.id(entity.id),
      update = pide.write(entity),
      writeConcern = writeConcern)
  }

}
