package net.fehmicansaglam.bson

import java.nio.ByteOrder

trait BsonValue extends Writable {
  implicit val byteOrder = ByteOrder.LITTLE_ENDIAN

  def pretty(level: Int): String = toString

  /** Overloaded empty paren method because of java interop. */
  def pretty(): String = pretty(0)
}
