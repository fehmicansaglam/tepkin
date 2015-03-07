package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonObjectId

case class BsonObjectIdReader(buffer: ByteBuffer) extends Reader[BsonObjectId] {
  override def read: Option[BsonObjectId] = {
    val name = readCString()
    val value = readBytes(12)
    Some(BsonObjectId(name, value))
  }
}
