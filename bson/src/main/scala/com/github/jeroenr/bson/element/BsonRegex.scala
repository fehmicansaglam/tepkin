package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueRegex

case class BsonRegex(name: String, value: BsonValueRegex) extends BsonElement {
  val code: Byte = 0x0B
}
