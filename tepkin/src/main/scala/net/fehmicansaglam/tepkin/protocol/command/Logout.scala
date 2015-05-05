package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._

/**
 * Terminates the current authenticated session
 */
case class Logout(databaseName: String) extends Command {
  override val command: BsonDocument = "logout" := 1
}
