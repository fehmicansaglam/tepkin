package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.Implicits.BsonValueTimestamp
import net.fehmicansaglam.bson.element.BsonTimestamp

object BsonTimestampReader extends Reader[BsonTimestamp] {

  def read(buffer: ByteBuffer): Option[BsonTimestamp] = {
    val name = readCString(buffer)
    val increment = buffer.getInt
    val timestamp = buffer.getInt
    Some(BsonTimestamp(name, new BsonValueTimestamp(increment, timestamp)))
  }
}
