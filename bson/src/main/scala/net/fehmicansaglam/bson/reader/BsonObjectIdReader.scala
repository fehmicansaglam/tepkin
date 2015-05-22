package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonObjectId

object BsonObjectIdReader extends Reader[BsonObjectId] {
  override def read(buffer: ByteBuffer): Option[BsonObjectId] = {
    val name = readCString(buffer)
    val value = readBytes(buffer)(12)
    Some(BsonObjectId(name, value))
  }
}
