package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonObject

object BsonObjectReader extends Reader[BsonObject] {

  def read(buffer: ByteBuffer): Option[BsonObject] = {
    val name = readCString(buffer)
    BsonDocumentReader.read(buffer).map(BsonObject(name, _))
  }
}
