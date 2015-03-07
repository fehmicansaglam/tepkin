package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonNull

case class BsonNullReader(buffer: ByteBuffer) extends Reader[BsonNull] {
  override def read: Option[BsonNull] = {
    Some(BsonNull(readCString()))
  }
}
