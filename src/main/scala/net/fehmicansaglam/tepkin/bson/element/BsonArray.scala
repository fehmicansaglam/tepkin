package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueArray

case class BsonArray(name: String, value: BsonValueArray) extends BsonElement {
  val code: Byte = 0x04
}
