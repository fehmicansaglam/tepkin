package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import Implicits.BsonValueTimestamp

case class BsonTimestamp(name: String, value: BsonValueTimestamp) extends BsonElement {
  val code: Byte = 0x11
}
