package com.github.jeroenr.bson.reader

import java.nio.ByteBuffer

import com.github.jeroenr.bson.element.BsonNull

object BsonNullReader extends Reader[BsonNull] {
  override def read(buffer: ByteBuffer): Option[BsonNull] = {
    Some(BsonNull(readCString(buffer)))
  }
}
