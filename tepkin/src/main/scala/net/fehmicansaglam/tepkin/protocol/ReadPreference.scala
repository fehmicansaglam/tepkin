package net.fehmicansaglam.tepkin.protocol

sealed trait ReadPreference

object ReadPreference {

  case object Primary extends ReadPreference {
    override def toString = "primary"
  }

  case object PrimaryPreferred extends ReadPreference {
    override def toString = "primaryPreferred"
  }

  case object Secondary extends ReadPreference {
    override def toString = "secondary"
  }

  case object SecondaryPreferred extends ReadPreference {
    override def toString = "secondaryPreferred"
  }

  case object Nearest extends ReadPreference {
    override def toString = "nearest"
  }

  def apply(str: String): ReadPreference = {
    str match {
      case "primary" => Primary
      case "primaryPreferred" => PrimaryPreferred
      case "secondary" => Secondary
      case "secondaryPreferred" => SecondaryPreferred
      case "nearest" => Nearest
      case _ ⇒ Primary
    }
  }
}
