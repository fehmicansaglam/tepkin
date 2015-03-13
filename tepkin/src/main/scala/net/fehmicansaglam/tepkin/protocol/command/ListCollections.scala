package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

case class ListCollections(databaseName: String, filter: Option[BsonDocument] = None) extends Command {
  override val command: BsonDocument = ("listCollections" := 1) ~ ("filter" := filter)
}
