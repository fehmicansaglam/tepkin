package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import Implicits.BsonValueInteger

case class BsonInteger(name: String, value: BsonValueInteger) extends BsonElement {
  val code: Byte = 0x10
}
