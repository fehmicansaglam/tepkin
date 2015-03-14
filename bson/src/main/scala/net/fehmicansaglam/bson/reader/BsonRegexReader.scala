package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.Implicits.BsonValueRegex
import net.fehmicansaglam.bson.element.BsonRegex

case class BsonRegexReader(buffer: ByteBuffer) extends Reader[BsonRegex] {

  def read: Option[BsonRegex] = {
    val name = readCString()
    val pattern = readCString()
    val options = readCString()
    Some(BsonRegex(name, BsonValueRegex(pattern, options)))
  }
}
