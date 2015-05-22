package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonArray

object BsonArrayReader extends Reader[BsonArray] {

  def read(buffer: ByteBuffer): Option[BsonArray] = {
    val name = readCString(buffer)
    BsonDocumentReader.read(buffer).map(BsonArray(name, _))
  }
}
