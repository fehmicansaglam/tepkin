package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueTimestamp
import net.fehmicansaglam.tepkin.bson.element.BsonTimestamp


case class BsonTimestampReader(buffer: ByteBuffer) extends Reader[BsonTimestamp] {

  def read: Option[BsonTimestamp] = {
    val name = readCString()
    val value = buffer.getLong()
    Some(BsonTimestamp(name, new BsonValueTimestamp(value)))
  }
}
