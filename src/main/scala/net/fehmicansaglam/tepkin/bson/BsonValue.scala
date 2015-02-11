package net.fehmicansaglam.tepkin.bson

import java.nio.ByteOrder

trait BsonValue extends Writable {
  implicit val byteOrder = ByteOrder.LITTLE_ENDIAN
}
