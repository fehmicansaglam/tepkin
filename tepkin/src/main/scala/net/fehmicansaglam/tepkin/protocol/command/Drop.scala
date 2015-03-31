package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

/**
 * Removes an entire collection from a database.
 * This command also removes any indexes associated with the dropped collection.
 */
case class Drop(databaseName: String, collectionName: String) extends Command {
  override val command: BsonDocument = "drop" := collectionName
}
