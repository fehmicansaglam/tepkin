package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueArray

case class BsonArray(name: String, value: BsonValueArray) extends BsonElement {
  val code: Byte = 0x04
}
