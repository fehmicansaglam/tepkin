package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonNull

object BsonNullReader extends Reader[BsonNull] {
  override def read(buffer: ByteBuffer): Option[BsonNull] = {
    Some(BsonNull(readCString(buffer)))
  }
}
