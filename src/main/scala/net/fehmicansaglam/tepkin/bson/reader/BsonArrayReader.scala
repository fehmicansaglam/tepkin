package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonArray

case class BsonArrayReader(buffer: ByteBuffer) extends Reader[BsonArray] {

  def read: Option[BsonArray] = {
    val name = readCString()
    BsonDocumentReader(buffer).read.map(BsonArray(name, _))
  }
}
