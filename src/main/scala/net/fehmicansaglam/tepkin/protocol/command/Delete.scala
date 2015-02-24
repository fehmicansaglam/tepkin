package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._

case class Delete(databaseName: String,
                  collectionName: String,
                  deletes: Seq[BsonDocument],
                  ordered: Boolean = true,
                  writeConcern: Option[BsonDocument] = None) extends Command {
  override val command: BsonDocument = {
    ("delete" := collectionName) ~
      ("deletes" := $array(deletes: _*)) ~
      ("ordered" := ordered) ~
      writeConcern.map("writeConcern" := _)
  }
}
