package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonLong

object BsonLongReader extends Reader[BsonLong] {

  def read(buffer: ByteBuffer): Option[BsonLong] = {
    val name = readCString(buffer)
    val value = buffer.getLong()
    Some(BsonLong(name, value))
  }
}
