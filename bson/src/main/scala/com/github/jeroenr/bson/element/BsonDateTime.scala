package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueDateTime

case class BsonDateTime(name: String, value: BsonValueDateTime) extends BsonElement {
  val code: Byte = 0x09
}
