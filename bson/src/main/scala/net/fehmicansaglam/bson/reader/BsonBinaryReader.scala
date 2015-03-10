package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import akka.util.ByteString
import net.fehmicansaglam.bson.Implicits.BsonValueBinary
import net.fehmicansaglam.bson.element.{BinarySubtype, BsonBinary}

case class BsonBinaryReader(buffer: ByteBuffer) extends Reader[BsonBinary] {

  def read: Option[BsonBinary] = {
    val name = readCString()
    val length = buffer.getInt()
    val subtype = buffer.get()
    val value = readBytes(length)
    Some(BsonBinary(name, BsonValueBinary(ByteString(value), BinarySubtype(subtype))))
  }
}
