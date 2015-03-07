package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.{BsonDocument, BsonDsl, Implicits}
import BsonDsl._
import Implicits._

case object IsMaster extends AdminCommand {
  override val command: BsonDocument = "isMaster" := 1
}
