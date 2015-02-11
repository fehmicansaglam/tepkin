package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonDouble

case class BsonDoubleReader(buffer: ByteBuffer) extends Reader[BsonDouble] {

  def read: Option[BsonDouble] = {
    val name = readCString()
    val value = buffer.getDouble()
    Some(BsonDouble(name, value))
  }
}
