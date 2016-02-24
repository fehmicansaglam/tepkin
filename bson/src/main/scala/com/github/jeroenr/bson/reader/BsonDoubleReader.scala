package com.github.jeroenr.bson.reader

import java.nio.ByteBuffer

import com.github.jeroenr.bson.element.BsonDouble

object BsonDoubleReader extends Reader[BsonDouble] {

  def read(buffer: ByteBuffer): Option[BsonDouble] = {
    val name = readCString(buffer)
    val value = buffer.getDouble()
    Some(BsonDouble(name, value))
  }
}
