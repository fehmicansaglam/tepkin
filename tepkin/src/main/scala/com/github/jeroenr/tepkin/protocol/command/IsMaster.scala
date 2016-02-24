package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}

case object IsMaster extends AdminCommand {
  override val command: BsonDocument = "isMaster" := 1
}
