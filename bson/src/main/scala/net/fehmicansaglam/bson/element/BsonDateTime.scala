package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import Implicits.BsonValueDateTime

case class BsonDateTime(name: String, value: BsonValueDateTime) extends BsonElement {
  val code: Byte = 0x09
}
