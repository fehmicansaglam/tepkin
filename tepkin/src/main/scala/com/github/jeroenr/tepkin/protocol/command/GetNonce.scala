package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}

/**
 * Use getnonce to generate a one-time password for authentication.
 */
case class GetNonce(databaseName: String) extends Command {
  override val command: BsonDocument = "getnonce" := 1
}
