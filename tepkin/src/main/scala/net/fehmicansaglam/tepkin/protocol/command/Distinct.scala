package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

/**
 * Finds the distinct values for a specified field across a single collection.
 *
 * @param collectionName The name of the collection to query for distinct values.
 * @param key The field for which to return distinct values.
 * @param query Optional. A query that specifies the documents from which to retrieve the distinct values.
 */
case class Distinct(databaseName: String,
                    collectionName: String,
                    key: String,
                    query: Option[BsonDocument] = None) extends Command {
  override def command: BsonDocument = {
    ("distinct" := collectionName) ~
      ("key" := key) ~
      ("query" := query)
  }
}
