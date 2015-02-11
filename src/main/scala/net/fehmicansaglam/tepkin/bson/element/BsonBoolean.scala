package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueBoolean

case class BsonBoolean(name: String, value: BsonValueBoolean) extends BsonElement {
  val code: Byte = 0x08
}
