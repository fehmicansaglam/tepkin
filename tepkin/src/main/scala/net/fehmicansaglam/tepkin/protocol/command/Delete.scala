package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.{BsonDocument, BsonDsl, Implicits}
import BsonDsl._
import Implicits._

case class Delete(databaseName: String,
                  collectionName: String,
                  deletes: Seq[DeleteElement],
                  ordered: Option[Boolean] = None,
                  writeConcern: Option[BsonDocument] = None) extends Command {
  override val command: BsonDocument = {
    ("delete" := collectionName) ~
      ("deletes" := $array(deletes.map(_.asBsonDocument): _*)) ~
      ("ordered" := ordered) ~
      ("writeConcern" := writeConcern)
  }
}

case class DeleteElement(q: BsonDocument, limit: Option[Int] = None) {
  val asBsonDocument: BsonDocument = ("q" := q) ~ ("limit" := limit)
}
