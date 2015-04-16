package net.fehmicansaglam.bson.util

import java.util.Base64

trait Codec {

  def decodeUtf8(value: String): Array[Byte] = {
    value.getBytes("UTF-8")
  }

  def encodeUtf8(value: Array[Byte]): String = {
    new String(value, "UTF-8")
  }

  def encodeBase64(value: Array[Byte]): String = {
    encodeUtf8(Base64.getEncoder.encode(value))
  }

  def encodeBase64(value: String): String = {
    encodeBase64(decodeUtf8(value))
  }

  def decodeBase64(value: String): Array[Byte] = {
    Base64.getDecoder.decode(value)
  }

}

object Codec extends Codec
