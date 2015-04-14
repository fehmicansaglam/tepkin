package net.fehmicansaglam.tepkin.protocol.exception

import net.fehmicansaglam.tepkin.protocol.result.WriteConcernError

case class WriteConcernException(writeConcernError: WriteConcernError) extends TepkinException
