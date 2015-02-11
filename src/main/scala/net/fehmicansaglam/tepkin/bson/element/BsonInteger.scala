package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueInteger

case class BsonInteger(name: String, value: BsonValueInteger) extends BsonElement {
  val code: Byte = 0x10
}
