package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._

case class Delete(databaseName: String, collectionName: String, deletes: Seq[BsonDocument]) extends Command {
  override val command: BsonDocument = ("delete" := collectionName) ~ ("deletes" := array(deletes: _*))
}
