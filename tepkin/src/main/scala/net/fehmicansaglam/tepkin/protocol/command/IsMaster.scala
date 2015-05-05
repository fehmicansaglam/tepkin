package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._

case object IsMaster extends AdminCommand {
  override val command: BsonDocument = "isMaster" := 1
}
