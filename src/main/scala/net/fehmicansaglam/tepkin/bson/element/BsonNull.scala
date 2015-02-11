package net.fehmicansaglam.tepkin.bson.element

import akka.util.ByteString
import net.fehmicansaglam.tepkin.bson.BsonValue

case class BsonNull(name: String) extends BsonElement {

  val code: Byte = 0x0A

  val value = new BsonValue {
    override def encode(): ByteString = ByteString.empty

    override def toString(): String = "null"
  }
}
