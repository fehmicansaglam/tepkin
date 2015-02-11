package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._

case class Insert(databaseName: String,
                  collectionName: String,
                  documents: Seq[BsonDocument],
                  ordered: Boolean = true,
                  writeConcern: Option[BsonDocument] = None) extends Command {

  override val command: BsonDocument = {
    ("insert" := collectionName) ~
      ("documents" := array(documents: _*)) ~
      ("ordered" := ordered) ~
      writeConcern.map("writeConcern" := _)
  }
}
