package com.github.jeroenr.tepkin

sealed trait TepkinMessage

object TepkinMessage {

  case object Init extends TepkinMessage

  case object Idle extends TepkinMessage

  case object ShutDown extends TepkinMessage

  case object ConnectFailed extends TepkinMessage

  case object WriteFailed extends TepkinMessage

  case object ConnectionClosed extends TepkinMessage

  case object Fetch extends TepkinMessage

  case object WhatsYourVersion extends TepkinMessage

  case class CursorOpened(cursorID: Long) extends TepkinMessage

  case class CursorClosed(cursorID: Long) extends TepkinMessage

}
