package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits.BsonValueRegex

case class BsonRegex(name: String, value: BsonValueRegex) extends BsonElement {
  val code: Byte = 0x0B
}
