package net.fehmicansaglam.bson.reader

import java.nio.ByteBuffer

import scala.collection.mutable.ArrayBuffer

trait Reader[T] {

  /**
   * @param buffer must have LITTLE_ENDIAN order
   */
  def readCString(buffer: ByteBuffer): String = readCString(buffer, new ArrayBuffer[Byte](16))

  @scala.annotation.tailrec
  private def readCString(buffer: ByteBuffer, array: ArrayBuffer[Byte]): String = {
    val byte = buffer.get()
    if (byte == 0x00)
      new String(array.toArray, "UTF-8")
    else readCString(buffer, array += byte)
  }

  /**
   * @param buffer must have LITTLE_ENDIAN order
   */
  def readString(buffer: ByteBuffer): String = {
    val size = buffer.getInt()
    val array = new Array[Byte](size - 1)
    buffer.get(array)
    buffer.get()
    new String(array)
  }

  /**
   * @param buffer must have LITTLE_ENDIAN order
   */
  def readBytes(buffer: ByteBuffer)(num: Int): Array[Byte] = {
    val array = new Array[Byte](num)
    buffer.get(array)
    array
  }

  /**
   * @param buffer must have LITTLE_ENDIAN order
   */
  def read(buffer: ByteBuffer): Option[T]
}
