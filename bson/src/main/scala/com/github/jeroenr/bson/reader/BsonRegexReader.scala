package com.github.jeroenr.bson.reader

import java.nio.ByteBuffer

import com.github.jeroenr.bson.Implicits.BsonValueRegex
import com.github.jeroenr.bson.element.BsonRegex

object BsonRegexReader extends Reader[BsonRegex] {

  def read(buffer: ByteBuffer): Option[BsonRegex] = {
    val name = readCString(buffer)
    val pattern = readCString(buffer)
    val options = readCString(buffer)
    Some(BsonRegex(name, BsonValueRegex(pattern, options)))
  }
}
