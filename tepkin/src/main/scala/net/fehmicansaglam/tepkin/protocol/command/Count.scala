package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.{BsonDocument, BsonDsl, Implicits}

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
