package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueLong

case class BsonLong(name: String, value: BsonValueLong) extends BsonElement {
  val code: Byte = 0x12
}
