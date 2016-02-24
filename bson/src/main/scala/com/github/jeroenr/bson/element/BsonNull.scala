package com.github.jeroenr.bson.element

import akka.util.ByteString
import com.github.jeroenr.bson.BsonValue

case class BsonNull(name: String) extends BsonElement {

  val code: Byte = 0x0A

  val value = new BsonValue {
    override def encode: ByteString = ByteString.empty

    override def toString: String = "null"
  }

  override def equals(other: Any): Boolean = {
    other.isInstanceOf[BsonNull] && other.asInstanceOf[BsonNull].name == this.name
  }
}
