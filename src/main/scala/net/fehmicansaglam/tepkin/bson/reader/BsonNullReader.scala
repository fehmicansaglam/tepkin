package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonNull

case class BsonNullReader(buffer: ByteBuffer) extends Reader[BsonNull] {
  override def read: Option[BsonNull] = {
    Some(BsonNull(readCString()))
  }
}
