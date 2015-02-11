package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonString

case class BsonStringReader(buffer: ByteBuffer) extends Reader[BsonString] {

  def read: Option[BsonString] = {
    val name = readCString()
    val value = readString()
    Some(BsonString(name, value))
  }
}
