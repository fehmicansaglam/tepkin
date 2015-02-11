package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueString

case class BsonString(name: String, value: BsonValueString) extends BsonElement {
  val code: Byte = 0x02
}
