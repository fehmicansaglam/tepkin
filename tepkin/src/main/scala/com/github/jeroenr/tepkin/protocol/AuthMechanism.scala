package com.github.jeroenr.tepkin.protocol

sealed trait AuthMechanism

object AuthMechanism {

  case object PLAIN extends AuthMechanism {
    override def toString: String = "PLAIN"
  }

  case object MONGODB_CR extends AuthMechanism {
    override def toString: String = "MONGODB-CR"
  }

  case object SCRAM_SHA_1 extends AuthMechanism {
    override def toString: String = "SCRAM-SHA-1"
  }

  def apply(str: String): AuthMechanism = {
    str match {
      case "PLAIN" => PLAIN
      case "MONGODB-CR" => MONGODB_CR
      case "SCRAM-SHA-1" => SCRAM_SHA_1
    }
  }
}
