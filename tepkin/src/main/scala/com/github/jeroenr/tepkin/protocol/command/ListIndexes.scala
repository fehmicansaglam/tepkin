package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}

case class ListIndexes(databaseName: String,
                       collectionName: String) extends Command {
  override def command: BsonDocument = "listIndexes" := collectionName
}
