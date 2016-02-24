package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueObject

case class BsonObject(name: String, value: BsonValueObject) extends BsonElement {
  val code: Byte = 0x03
}
