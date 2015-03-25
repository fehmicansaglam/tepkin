package net.fehmicansaglam.pide

import net.fehmicansaglam.bson.BsonDocument

trait Pide[E <: Entity] {

  def read(document: BsonDocument): E

  def write(entity: E): BsonDocument
}
