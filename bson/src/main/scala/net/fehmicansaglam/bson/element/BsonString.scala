package net.fehmicansaglam.bson.element

import net.fehmicansaglam.bson.Implicits
import Implicits.BsonValueString

case class BsonString(name: String, value: BsonValueString) extends BsonElement {
  val code: Byte = 0x02
}
