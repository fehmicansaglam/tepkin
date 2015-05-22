package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonBoolean

object BsonBooleanReader extends Reader[BsonBoolean] {

  def read(buffer: ByteBuffer): Option[BsonBoolean] = {
    val name = readCString(buffer)
    Some {
      buffer.get() match {
        case 0x00 => BsonBoolean(name, false)
        case 0x01 => BsonBoolean(name, true)
      }
    }
  }
}
