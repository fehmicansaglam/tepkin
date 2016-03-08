package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueBoolean

case class BsonBoolean(name: String, value: BsonValueBoolean) extends BsonElement {
  val code: Byte = 0x08
}
