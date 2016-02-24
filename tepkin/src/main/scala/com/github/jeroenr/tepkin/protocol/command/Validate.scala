package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}
import com.github.jeroenr.bson.Implicits._

case class Validate(databaseName: String,
                    collectionName: String,
                    full: Option[Boolean] = None,
                    scandata: Option[Boolean] = None) extends Command {
  override def command: BsonDocument = {
    ("validate" := collectionName) ~
      ("full" := full) ~
      ("scandata" := scandata)
  }
}
