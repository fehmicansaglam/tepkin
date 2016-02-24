package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}

/**
 * Terminates the current authenticated session
 */
case class Logout(databaseName: String) extends Command {
  override val command: BsonDocument = "logout" := 1
}
