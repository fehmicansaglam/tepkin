package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.bson.element.BsonDateTime
import org.joda.time.DateTime

object BsonDateTimeReader extends Reader[BsonDateTime] {

  override def read(buffer: ByteBuffer): Option[BsonDateTime] = {
    val name = readCString(buffer)
    val value = buffer.getLong()
    Some(BsonDateTime(name, new DateTime(value)))
  }
}
