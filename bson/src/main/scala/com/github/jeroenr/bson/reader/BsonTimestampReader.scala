package com.github.jeroenr.bson.reader

import java.nio.ByteBuffer

import com.github.jeroenr.bson.Implicits.BsonValueTimestamp
import com.github.jeroenr.bson.element.BsonTimestamp

object BsonTimestampReader extends Reader[BsonTimestamp] {

  def read(buffer: ByteBuffer): Option[BsonTimestamp] = {
    val name = readCString(buffer)
    val increment = buffer.getInt
    val timestamp = buffer.getInt
    Some(BsonTimestamp(name, new BsonValueTimestamp(increment, timestamp)))
  }
}
