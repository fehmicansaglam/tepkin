package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

/**
 * Retrieve information, i.e. the name and options, about the collections in a database.
 *
 * @since MongoDB 3.0.0
 *
 * @param filter Optional. A query expression to filter the list of collections.
 */
case class ListCollections(databaseName: String, filter: Option[BsonDocument] = None) extends Command {
  override val command: BsonDocument = ("listCollections" := 1) ~ ("filter" := filter)
}
