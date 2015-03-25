package net.fehmicansaglam.pide

import net.fehmicansaglam.bson.Implicits.BsonValueObjectId

trait Entity {
  def id: BsonValueObjectId
}
