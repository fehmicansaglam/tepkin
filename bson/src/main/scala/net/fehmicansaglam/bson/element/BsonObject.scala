package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import Implicits.BsonValueObject

case class BsonObject(name: String, value: BsonValueObject) extends BsonElement {
  val code: Byte = 0x03
}
