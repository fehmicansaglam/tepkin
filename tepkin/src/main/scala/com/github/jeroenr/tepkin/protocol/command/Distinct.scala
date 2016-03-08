package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}

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
