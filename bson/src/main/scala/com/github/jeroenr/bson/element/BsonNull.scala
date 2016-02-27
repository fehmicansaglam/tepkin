package com.github.jeroenr.bson.element

import akka.util.ByteString
import com.github.jeroenr.bson.{Identifiable, BsonValue}

case object BsonNullValue extends BsonValue with Identifiable[Null] {
  override def identifier: Null = null
  override def encode: ByteString = ByteString.empty
  override def toString: String = "null"
}

case class BsonNull(name: String) extends BsonElement {

  val code: Byte = 0x0A

  val value = BsonNullValue

  override def equals(other: Any): Boolean = {
    other.isInstanceOf[BsonNull] && other.asInstanceOf[BsonNull].name == this.name
  }
}
