package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueObject

case class BsonObject(name: String, value: BsonValueObject) extends BsonElement {
  val code: Byte = 0x03
}
