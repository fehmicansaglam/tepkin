package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueDouble

case class BsonDouble(name: String, value: BsonValueDouble) extends BsonElement {
  val code: Byte = 0x01
}
