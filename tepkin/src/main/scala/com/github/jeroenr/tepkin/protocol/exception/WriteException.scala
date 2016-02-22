package com.github.jeroenr.tepkin.protocol.exception

import com.github.jeroenr.tepkin.protocol.result.WriteError

case class WriteException(writeErrors: List[WriteError]) extends TepkinException
