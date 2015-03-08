package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits.BsonValueBinary

case class BsonBinary(name: String, value: BsonValueBinary) extends BsonElement {
  val code: Byte = 0x05
}
