package net.fehmicansaglam.bson.element

import akka.util.{ByteString, ByteStringBuilder}
import net.fehmicansaglam.bson.{BsonDocument, BsonValue, Writable}

trait BsonElement extends Writable {

  def code: Byte

  def name: String

  def value: BsonValue

  override def encode: ByteString = {
    val builder = new ByteStringBuilder
    builder.putByte(code)
    putCString(builder, name)
    builder.append(value.encode)
    builder.result()
  }

  override def toString: String = s""" "$name": $value """.trim

  override def equals(other: Any): Boolean = {
    other.isInstanceOf[BsonElement] &&
      other.asInstanceOf[BsonElement].code == this.code &&
      other.asInstanceOf[BsonElement].name == this.name &&
      other.asInstanceOf[BsonElement].value == this.value
  }

  def ~(that: BsonElement): BsonDocument = BsonDocument(this, that)

  def ~(that: Option[BsonElement]): BsonDocument = that match {
    case Some(_that) => this ~ _that
    case None => BsonDocument(this)
  }

  def toDoc: BsonDocument = BsonDocument(this)

  def pretty(level: Int): String = {
    val prefix = "\t" * level
    s"""$prefix"$name": ${value.pretty(level)}"""
  }

  /** Overloaded empty paren method because of java interop. */
  def pretty(): String = pretty(0)

  def toJson(extended: Boolean): String = s""" "$name": ${value.toJson(extended)} """.trim

  def toTuple: (String, BsonValue) = name -> value
}
