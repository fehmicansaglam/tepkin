package com.github.jeroenr.tepkin.protocol.exception

import com.github.jeroenr.tepkin.protocol.result.WriteConcernError

case class WriteConcernException(writeConcernError: WriteConcernError) extends TepkinException
