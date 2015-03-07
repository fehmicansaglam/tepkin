package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

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
