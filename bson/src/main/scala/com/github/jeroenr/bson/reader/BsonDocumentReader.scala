package com.github.jeroenr.bson.reader

import java.nio.ByteBuffer

import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.bson.element.BsonElement

import scala.collection.mutable.ArrayBuffer
import scala.util.control.Breaks._

object BsonDocumentReader extends Reader[BsonDocument] {

  private def readElement(buffer: ByteBuffer, code: Byte): Option[BsonElement] = code match {
    case 0x01 => BsonDoubleReader.read(buffer)
    case 0x02 => BsonStringReader.read(buffer)
    case 0x03 => BsonObjectReader.read(buffer)
    case 0x04 => BsonArrayReader.read(buffer)
    case 0x05 => BsonBinaryReader.read(buffer)
    case 0x07 => BsonObjectIdReader.read(buffer)
    case 0x08 => BsonBooleanReader.read(buffer)
    case 0x09 => BsonDateTimeReader.read(buffer)
    case 0x0A => BsonNullReader.read(buffer)
    case 0x0B => BsonRegexReader.read(buffer)
    case 0x10 => BsonIntegerReader.read(buffer)
    case 0x11 => BsonTimestampReader.read(buffer)
    case 0x12 => BsonLongReader.read(buffer)
  }

  override def read(buffer: ByteBuffer): Option[BsonDocument] = {
    val elements: ArrayBuffer[Option[BsonElement]] = new ArrayBuffer[Option[BsonElement]]
    val size = buffer.getInt()

    breakable {
      while (buffer.hasRemaining) {
        val code = buffer.get()
        if (code != 0x00) {
          elements += readElement(buffer, code)
        } else {
          break
        }
      }
    }

    Some(BsonDocument(elements.flatten: _*))
  }

  def read(array: Array[Byte]): Option[BsonDocument] = read(ByteBuffer.wrap(array))
}

