package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}

/**
 * Removes an entire collection from a database.
 * This command also removes any indexes associated with the dropped collection.
 */
case class Drop(databaseName: String, collectionName: String) extends Command {
  override val command: BsonDocument = "drop" := collectionName
}
