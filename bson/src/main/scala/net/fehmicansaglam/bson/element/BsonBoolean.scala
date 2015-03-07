package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import Implicits.BsonValueBoolean

case class BsonBoolean(name: String, value: BsonValueBoolean) extends BsonElement {
  val code: Byte = 0x08
}
