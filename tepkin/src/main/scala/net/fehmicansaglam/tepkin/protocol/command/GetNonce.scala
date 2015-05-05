package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._

/**
 * Use getnonce to generate a one-time password for authentication.
 */
case class GetNonce(databaseName: String) extends Command {
  override val command: BsonDocument = "getnonce" := 1
}
