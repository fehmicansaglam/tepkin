package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.Implicits.BsonValueRegex
import net.fehmicansaglam.bson.element.BsonRegex

object BsonRegexReader extends Reader[BsonRegex] {

  def read(buffer: ByteBuffer): Option[BsonRegex] = {
    val name = readCString(buffer)
    val pattern = readCString(buffer)
    val options = readCString(buffer)
    Some(BsonRegex(name, BsonValueRegex(pattern, options)))
  }
}
