package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonLong


case class BsonLongReader(buffer: ByteBuffer) extends Reader[BsonLong] {

  def read: Option[BsonLong] = {
    val name = readCString()
    val value = buffer.getLong()
    Some(BsonLong(name, value))
  }
}
