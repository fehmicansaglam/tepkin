package net.fehmicansaglam.bson.element

trait BinarySubtype {
  def code: Byte
}

object BinarySubtype {

  case object Generic extends BinarySubtype {
    val code: Byte = 0x00
  }

  case object Function extends BinarySubtype {
    val code: Byte = 0x01
  }

  case object DeprecatedGeneric extends BinarySubtype {
    val code: Byte = 0x02
  }

  case object DeprecatedUUID extends BinarySubtype {
    val code: Byte = 0x03
  }

  case object UUID extends BinarySubtype {
    val code: Byte = 0x04
  }

  case object MD5 extends BinarySubtype {
    val code: Byte = 0x05
  }

  case object UserDefined extends BinarySubtype {
    val code: Byte = 0x80.toByte
  }

  def apply(code: Byte): BinarySubtype = code match {
    case 0x00 => Generic
    case 0x01 => Function
    case 0x02 => DeprecatedGeneric
    case 0x03 => DeprecatedUUID
    case 0x04 => UUID
    case 0x05 => MD5
    case 0x80 => UserDefined
    case _ ⇒ throw new IllegalArgumentException(code.toString)
  }
}
