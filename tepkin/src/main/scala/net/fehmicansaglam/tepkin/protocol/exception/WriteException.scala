package net.fehmicansaglam.tepkin.protocol.exception

import net.fehmicansaglam.tepkin.protocol.result.WriteError

case class WriteException(writeErrors: List[WriteError]) extends TepkinException
