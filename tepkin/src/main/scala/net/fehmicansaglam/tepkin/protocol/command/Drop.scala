package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.{BsonDocument, BsonDsl, Implicits}
import BsonDsl._
import Implicits._

case class Drop(databaseName: String, collectionName: String) extends Command {
  override val command: BsonDocument = "drop" := collectionName
}
