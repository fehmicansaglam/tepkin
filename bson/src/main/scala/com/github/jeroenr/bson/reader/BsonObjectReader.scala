package com.github.jeroenr.bson.reader

import java.nio.ByteBuffer

import com.github.jeroenr.bson.element.BsonObject

object BsonObjectReader extends Reader[BsonObject] {

  def read(buffer: ByteBuffer): Option[BsonObject] = {
    val name = readCString(buffer)
    BsonDocumentReader.read(buffer).map(BsonObject(name, _))
  }
}
