package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonString

object BsonStringReader extends Reader[BsonString] {

  def read(buffer: ByteBuffer): Option[BsonString] = {
    val name = readCString(buffer)
    val value = readString(buffer)
    Some(BsonString(name, value))
  }
}
