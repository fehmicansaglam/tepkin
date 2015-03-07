package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

case class Validate(databaseName: String,
                    collectionName: String,
                    full: Option[Boolean] = None,
                    scandata: Option[Boolean] = None) extends Command {
  override def command: BsonDocument = {
    ("validate" := collectionName) ~
      ("full" := full) ~
      ("scandata" := scandata)
  }
}
