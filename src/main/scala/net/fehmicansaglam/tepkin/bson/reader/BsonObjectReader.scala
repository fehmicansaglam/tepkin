package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonObject

case class BsonObjectReader(buffer: ByteBuffer) extends Reader[BsonObject] {

  def read: Option[BsonObject] = {
    val name = readCString()
    BsonDocumentReader(buffer).read.map(BsonObject(name, _))
  }
}
