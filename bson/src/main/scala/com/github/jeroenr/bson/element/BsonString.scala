package com.github.jeroenr.bson.element

import com.github.jeroenr.bson.Implicits.BsonValueString

case class BsonString(name: String, value: BsonValueString) extends BsonElement {
  val code: Byte = 0x02
}
