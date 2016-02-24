package com.github.jeroenr.bson

import akka.util.{ByteString, ByteStringBuilder}
import com.github.jeroenr.bson.element.BinarySubtype
import com.github.jeroenr.bson.util.{Codec, Converters}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

object Implicits {

  implicit class BsonValueDouble(value: Double) extends BsonValueNumber with Identifiable[Double] {

    override def identifier: Double = value

    override def encode: ByteString = new ByteStringBuilder().putDouble(value).result()

    override def toString: String = s"$value"

    override def toInt: Int = value.toInt

    override def toDouble: Double = value

    override def toLong: Long = value.toLong
  }

  object BsonValueDouble {
    def unapply(value: BsonValueDouble): Option[Double] = Some(value.identifier)
  }

  implicit class BsonValueString(value: String) extends BsonValue with Identifiable[String] {

    override def identifier: String = value

    override def encode: ByteString = {
      val builder = new ByteStringBuilder()
      val bytes = value.getBytes("utf-8")
      builder.putInt(bytes.length + 1)
      builder.putBytes(bytes)
      builder.putByte(0)
      builder.result()
    }

    override def toString: String = s""" "$value" """.trim
  }

  object BsonValueString {
    def unapply(value: BsonValueString): Option[String] = Some(value.identifier)
  }

  implicit class BsonValueObject(document: BsonDocument) extends BsonValue with Identifiable[BsonDocument] {

    override def identifier: BsonDocument = document

    override def encode: ByteString = document.encode

    override def toString: String = document.toString

    override def pretty(level: Int): String = document.pretty(level)

    override def toJson(extended: Boolean): String = document.toJson(extended)
  }

  implicit class BsonValueArray(document: BsonDocument) extends BsonValue with Identifiable[BsonDocument] {

    override def identifier: BsonDocument = document

    override def encode: ByteString = document.encode

    override def toString: String = s"[ ${document.elements.map(_.value).mkString(", ")} ]"

    override def pretty(level: Int): String = {
      val prefix = "\t" * (level + 1)
      val values = document.elements.map(_.value)
      val init = if (values.isEmpty) "" else values.init.foldLeft("")(_ + prefix + _.pretty(level + 1) + ",\n")
      val last = if (values.isEmpty) "" else prefix + values.last.pretty(level + 1)
      s"[\n$init$last\n${"\t" * level}]"
    }

    override def toJson(extended: Boolean): String = {
      s"[ ${document.elements.map(_.value.toJson(extended)).mkString(", ")} ]"
    }
  }

  implicit class BsonValueBoolean(value: Boolean) extends BsonValue with Identifiable[Boolean] {

    override def identifier: Boolean = value

    override def encode: ByteString = {
      val builder = new ByteStringBuilder()
      if (value) builder.putByte(0x01) else builder.putByte(0x00)
      builder.result()
    }

    override def toString: String = s"$value"
  }

  object BsonValueBoolean {
    def unapply(value: BsonValueBoolean): Option[Boolean] = Some(value.identifier)
  }

  implicit class BsonValueInteger(value: Int) extends BsonValueNumber with Identifiable[Int] {

    override def identifier: Int = value

    override def encode: ByteString = new ByteStringBuilder().putInt(value).result()

    override def toString: String = s"$value"

    override def toInt: Int = value

    override def toDouble: Double = value.toDouble

    override def toLong: Long = value.toLong
  }

  object BsonValueInteger {
    def unapply(value: BsonValueInteger): Option[Int] = Some(value.identifier)
  }

  implicit class BsonValueLong(value: Long) extends BsonValueNumber with Identifiable[Long] {

    override def identifier: Long = value

    override def encode: ByteString = new ByteStringBuilder().putLong(value).result()

    override def toString: String = s"$value"

    override def toInt: Int = value.toInt

    override def toDouble: Double = value.toDouble

    override def toLong: Long = value

    override def toJson(extended: Boolean): String = if (extended) {
      s"""{ "$$numberLong": "$value" }"""
    } else {
      value.toString
    }
  }

  object BsonValueLong {
    def unapply(value: BsonValueLong): Option[Long] = Some(value.identifier)
  }


  implicit class BsonValueObjectId(value: Array[Byte]) extends BsonValue with Identifiable[String] {

    override val identifier: String = Converters.hex2Str(value)

    override def encode: ByteString = ByteString.newBuilder.putBytes(value).result()

    override def toString: String = s"""ObjectId("$identifier")"""

    override def toJson(extended: Boolean): String = if (extended) {
      s"""{ "$$oid": "$identifier" }"""
    } else {
      s""" "$identifier" """.trim
    }
  }

  /**
   * Type alias for BsonValueObjectId
   */
  type ObjectId = BsonValueObjectId

  implicit class BsonValueDateTime(value: DateTime) extends BsonValue with Identifiable[DateTime] {

    override def identifier: DateTime = value

    override def encode: ByteString = ByteString.newBuilder.putLong(value.getMillis).result()

    override def toString: String = s"""ISODate("${ISODateTimeFormat.dateTime().print(value)}")"""

    override def toJson(extended: Boolean): String = if (extended) {
      s"""{ "$$date": { "$$numberLong": "${value.getMillis}" } }"""
    } else {
      value.getMillis.toString
    }
  }

  object BsonValueDateTime {
    def unapply(value: BsonValueDateTime): Option[DateTime] = Some(value.identifier)
  }

  case class BsonValueTimestamp(increment: Int, timestamp: Int) extends BsonValue with Identifiable[(Int, Int)] {

    override def identifier: (Int, Int) = (increment, timestamp)

    override def encode: ByteString = new ByteStringBuilder().putInt(increment).putInt(timestamp).result()

    override def toString: String = s"""{ "$$timestamp": { "t": $timestamp, "i": $increment } }"""
  }

  case class BsonValueBinary(value: ByteString, subtype: BinarySubtype) extends BsonValue with Identifiable[ByteString] {

    override def identifier: ByteString = value

    override def encode: ByteString = {
      ByteString.newBuilder
        .putInt(value.length)
        .putByte(subtype.code)
        .append(value)
        .result()
    }

    override def toJson(extended: Boolean): String = if (extended) {
      s"""{ "$$binary": "${Codec.encodeBase64(value.toArray)}", "$$type": "${subtype.code.formatted("%02x")}" }"""
    } else {
      Codec.encodeBase64(value.toArray)
    }
  }

  /**
   * Type alias for BsonValueBinary
   */
  val Binary = BsonValueBinary
  type Binary = BsonValueBinary

  case class BsonValueRegex(pattern: String, options: String) extends BsonValue with Identifiable[(String, String)] {
    override def identifier: (String, String) = (pattern, options)

    override def encode: ByteString = {
      val builder = ByteString.newBuilder
      putCString(builder, pattern)
      putCString(builder, options)
      builder.result()
    }

    override def toJson(extended: Boolean): String = s"""{ "$$regex": "$pattern", "$$options": "$options" }"""
  }

}
