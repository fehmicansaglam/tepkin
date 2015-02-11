package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._

case class Count(databaseName: String,
                 collectionName: String,
                 query: Option[BsonDocument] = None,
                 limit: Option[Int] = None,
                 skip: Option[Int] = None) extends Command {
  override val command: BsonDocument = {
    ("count" := collectionName) ~
      ("query" := query) ~
      ("limit" := limit) ~
      ("skip" := skip)
  }
}
