package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueBinary

case class BsonBinary(name: String, value: BsonValueBinary) extends BsonElement {
  val code: Byte = 0x05
}
