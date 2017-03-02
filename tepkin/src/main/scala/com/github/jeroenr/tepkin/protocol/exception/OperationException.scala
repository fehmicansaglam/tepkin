package com.github.jeroenr.tepkin.protocol.exception

import com.github.jeroenr.tepkin.protocol.result.OperationError

/**
 * Created by jero on 05/04/16.
 */
case class OperationException(error: OperationError) extends TepkinException
