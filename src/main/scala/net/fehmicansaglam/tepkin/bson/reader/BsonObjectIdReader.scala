package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonObjectId

case class BsonObjectIdReader(buffer: ByteBuffer) extends Reader[BsonObjectId] {
  override def read: Option[BsonObjectId] = {
    val name = readCString()
    val value = readBytes(12)
    Some(BsonObjectId(name, value))
  }
}
