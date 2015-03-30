package net.fehmicansaglam.pide

import akka.actor.ActorRef
import akka.stream.scaladsl.Source
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.tepkin.MongoCollection
import net.fehmicansaglam.tepkin.protocol.WriteConcern
import net.fehmicansaglam.tepkin.protocol.result.{InsertResult, UpdateResult}

import scala.concurrent.{ExecutionContext, Future}

trait Dao[ID, E <: Entity[ID]] {

  def collection: MongoCollection

  def find(query: BsonDocument, fields: Option[BsonDocument] = None, skip: Int = 0, batchMultiplier: Int = 1000)
          (implicit pide: Pide[ID, E], ec: ExecutionContext, timeout: Timeout): Future[Source[List[E], ActorRef]] = {
    collection.find(query, fields, skip, batchMultiplier).map(source => source.map(list => list.map(pide.read)))
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
