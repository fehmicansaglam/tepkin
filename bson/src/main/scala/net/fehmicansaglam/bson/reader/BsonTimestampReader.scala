package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.Implicits
import net.fehmicansaglam.bson.element.BsonTimestamp
import Implicits.BsonValueTimestamp


case class BsonTimestampReader(buffer: ByteBuffer) extends Reader[BsonTimestamp] {

  def read: Option[BsonTimestamp] = {
    val name = readCString()
    val value = buffer.getLong()
    Some(BsonTimestamp(name, new BsonValueTimestamp(value)))
  }
}
