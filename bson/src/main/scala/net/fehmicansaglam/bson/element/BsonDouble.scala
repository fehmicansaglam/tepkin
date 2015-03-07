package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import Implicits.BsonValueDouble

case class BsonDouble(name: String, value: BsonValueDouble) extends BsonElement {
  val code: Byte = 0x01
}
