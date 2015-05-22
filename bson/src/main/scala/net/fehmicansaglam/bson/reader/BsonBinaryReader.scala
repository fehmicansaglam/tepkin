package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import akka.util.ByteString
import net.fehmicansaglam.bson.Implicits.BsonValueBinary
import net.fehmicansaglam.bson.element.{BinarySubtype, BsonBinary}

object BsonBinaryReader extends Reader[BsonBinary] {

  def read(buffer: ByteBuffer): Option[BsonBinary] = {
    val name = readCString(buffer)
    val length = buffer.getInt()
    val subtype = buffer.get()
    val value = readBytes(buffer)(length)
    Some(BsonBinary(name, BsonValueBinary(ByteString(value), BinarySubtype(subtype))))
  }
}
