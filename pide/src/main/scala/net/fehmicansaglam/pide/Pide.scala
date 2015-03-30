package net.fehmicansaglam.pide

import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.{BsonDocument, BsonValue}

trait Pide[ID, E <: Entity[ID]] {

  def id(id: ID): BsonValue

  def read(document: BsonDocument): E

  def write(entity: E): BsonDocument
}

trait ObjectIdPide[E <: Entity[ObjectId]] extends Pide[ObjectId, E] {
  override def id(id: ObjectId): BsonValue = id
}

trait StringPide[E <: Entity[String]] extends Pide[String, E] {
  override def id(id: String): BsonValue = id
}

trait LongPide[E <: Entity[Long]] extends Pide[Long, E] {
  override def id(id: Long): BsonValue = id
}
