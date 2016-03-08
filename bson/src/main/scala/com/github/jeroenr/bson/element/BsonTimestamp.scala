package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueTimestamp

case class BsonTimestamp(name: String, value: BsonValueTimestamp) extends BsonElement {
  val code: Byte = 0x11
}
