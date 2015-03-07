package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import net.fehmicansaglam.bson.Implicits.BsonValueArray
import Implicits.BsonValueArray

case class BsonArray(name: String, value: BsonValueArray) extends BsonElement {
  val code: Byte = 0x04
}
