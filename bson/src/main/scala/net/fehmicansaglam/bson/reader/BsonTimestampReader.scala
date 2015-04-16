package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.Implicits
import net.fehmicansaglam.bson.element.BsonTimestamp
import Implicits.BsonValueTimestamp


case class BsonTimestampReader(buffer: ByteBuffer) extends Reader[BsonTimestamp] {

  def read: Option[BsonTimestamp] = {
    val name = readCString()
    val increment = buffer.getInt
    val timestamp = buffer.getInt
    Some(BsonTimestamp(name, new BsonValueTimestamp(increment, timestamp)))
  }
}
