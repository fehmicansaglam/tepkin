package net.fehmicansaglam.bson

import akka.util.{ByteString, ByteStringBuilder}
import net.fehmicansaglam.bson.util.Converters
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object Implicits {


  implicit class BsonValueDouble(value: Double) extends BsonNumber with Identifiable[Double] {

    override def identifier: Double = value

    override def encode(): ByteString = new ByteStringBuilder().putDouble(value).result()

    override def toString(): String = s"$value"

    override def toInt: Int = value.toInt

    override def toDouble: Double = value

    override def toLong: Long = value.toLong
  }

  implicit class BsonValueString(value: String) extends BsonValue with Identifiable[String] {

    override def identifier: String = value

    override def encode(): ByteString = {
      val builder = new ByteStringBuilder()
      val bytes = value.getBytes("utf-8")
      builder.putInt(bytes.length + 1)
      builder.putBytes(bytes)
      builder.putByte(0)
      builder.result()
    }

    override def toString(): String = s""" "$value" """.trim
  }

  implicit class BsonValueObject(document: BsonDocument) extends BsonValue with Identifiable[BsonDocument] {

    override def identifier: BsonDocument = document

    override def encode(): ByteString = document.encode

    override def toString(): String = document.toString()

    override def pretty(level: Int = 0): String = document.pretty(level)
  }

  implicit class BsonValueArray(document: BsonDocument) extends BsonValue with Identifiable[BsonDocument] {

    override def identifier: BsonDocument = document

    override def encode(): ByteString = document.encode

    override def toString(): String = s"[ ${document.elements.map(_.value).mkString(", ")} ]"

    override def pretty(level: Int = 0): String = {
      val prefix = "\t" * (level + 1)
      val values = document.elements.map(_.value)
      val init = if (values.isEmpty) "" else values.init.foldLeft("")(_ + prefix + _.pretty(level + 1) + s",\n")
      val last = if (values.isEmpty) "" else prefix + values.last.pretty(level + 1)
      s"[\n$init$last\n${"\t" * level}]"
    }
  }

  implicit class BsonValueBoolean(value: Boolean) extends BsonValue with Identifiable[Boolean] {

    override def identifier: Boolean = value

    override def encode(): ByteString = {
      val builder = new ByteStringBuilder()
      if (value) builder.putByte(0x01) else builder.putByte(0x00)
      builder.result()
    }

    override def toString(): String = s"$value"
  }

  implicit class BsonValueInteger(value: Int) extends BsonNumber with Identifiable[Int] {

    override def identifier: Int = value

    override def encode(): ByteString = new ByteStringBuilder().putInt(value).result()

    override def toString(): String = s"$value"

    override def toInt: Int = value

    override def toDouble: Double = value.toDouble

    override def toLong: Long = value.toLong
  }

  implicit class BsonValueLong(value: Long) extends BsonNumber with Identifiable[Long] {

    override def identifier: Long = value

    override def encode(): ByteString = new ByteStringBuilder().putLong(value).result()

    override def toString(): String = s"$value"

    override def toInt: Int = value.toInt

    override def toDouble: Double = value.toDouble

    override def toLong: Long = value
  }

  implicit class BsonValueObjectId(value: Array[Byte]) extends BsonValue with Identifiable[String] {

    override val identifier: String = Converters.hex2Str(value)

    override def encode(): ByteString = ByteString.newBuilder.putBytes(value).result()

    override def toString(): String = s"""ObjectId("$identifier")"""
  }

  implicit class BsonValueDateTime(value: DateTime) extends BsonValue with Identifiable[DateTime] {

    override def identifier: DateTime = value

    override def encode(): ByteString = ByteString.newBuilder.putLong(value.getMillis()).result()

    override def toString(): String = s"""ISODate("${ISODateTimeFormat.dateTime().print(value)}")"""
  }

  class BsonValueTimestamp(value: Long) extends BsonValue with Identifiable[Long] {

    override def identifier: Long = value

    override def encode(): ByteString = new ByteStringBuilder().putLong(value).result()

    override def toString(): String = s"$value"
  }

}
