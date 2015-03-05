package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueTimestamp

case class BsonTimestamp(name: String, value: BsonValueTimestamp) extends BsonElement {
  val code: Byte = 0x11
}
