package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueInteger

case class BsonInteger(name: String, value: BsonValueInteger) extends BsonElement {
  val code: Byte = 0x10
}
