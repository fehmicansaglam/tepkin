package net.fehmicansaglam.tepkin

object TepkinMessages {

  sealed trait TepkinMessage

  case object InitPool extends TepkinMessage

  case object Idle extends TepkinMessage

  case object ShutDown extends TepkinMessage

}