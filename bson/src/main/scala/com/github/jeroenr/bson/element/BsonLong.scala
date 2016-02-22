package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueLong

case class BsonLong(name: String, value: BsonValueLong) extends BsonElement {
  val code: Byte = 0x12
}
