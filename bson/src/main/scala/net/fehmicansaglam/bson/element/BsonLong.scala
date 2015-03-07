package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import Implicits.BsonValueLong

case class BsonLong(name: String, value: BsonValueLong) extends BsonElement {
  val code: Byte = 0x12
}
