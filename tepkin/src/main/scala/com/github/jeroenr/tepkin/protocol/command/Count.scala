package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonValue}

/**
 * Counts the number of documents in a collection.
 *
 * @param collectionName The name of the collection to count.
 * @param query Optional. A query that selects which documents to count in a collection.
 * @param limit Optional. The maximum number of matching documents to return.
 * @param skip Optional. The number of matching documents to skip before returning results.
 * @param hint Optional. The index to use. Specify either the index name as a string
 *             or the index specification document.
 */
case class Count(databaseName: String,
                 collectionName: String,
                 query: Option[BsonDocument] = None,
                 limit: Option[Int] = None,
                 skip: Option[Int] = None,
                 hint: Option[BsonValue] = None) extends Command {
  override val command: BsonDocument = {
    ("count" := collectionName) ~
      ("query" := query) ~
      ("limit" := limit) ~
      ("skip" := skip) ~
      ("hint" := hint)
  }
}
