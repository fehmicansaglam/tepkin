package com.github.jeroenr.bson.reader

import java.nio.ByteBuffer

import akka.util.ByteString
import com.github.jeroenr.bson.Implicits.BsonValueBinary
import com.github.jeroenr.bson.element.{BinarySubtype, BsonBinary}

object BsonBinaryReader extends Reader[BsonBinary] {

  def read(buffer: ByteBuffer): Option[BsonBinary] = {
    val name = readCString(buffer)
    val length = buffer.getInt()
    val subtype = buffer.get()
    val value = readBytes(buffer)(length)
    Some(BsonBinary(name, BsonValueBinary(ByteString(value), BinarySubtype(subtype))))
  }
}
