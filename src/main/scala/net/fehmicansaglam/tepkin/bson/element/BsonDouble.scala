package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueDouble

case class BsonDouble(name: String, value: BsonValueDouble) extends BsonElement {
  val code: Byte = 0x01
}
