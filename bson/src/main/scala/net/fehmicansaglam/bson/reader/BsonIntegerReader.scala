package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonInteger

object BsonIntegerReader extends Reader[BsonInteger] {

  def read(buffer: ByteBuffer): Option[BsonInteger] = {
    val name = readCString(buffer)
    val value = buffer.getInt()
    Some(BsonInteger(name, value))
  }
}
