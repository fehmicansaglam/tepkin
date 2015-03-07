package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.{BsonDocument, BsonDsl, Implicits}
import BsonDsl._
import Implicits._

case class FindAndModify(databaseName: String,
                         collectionName: String,
                         query: Option[BsonDocument] = None,
                         sort: Option[BsonDocument] = None,
                         removeOrUpdate: Either[Boolean, BsonDocument],
                         returnNew: Boolean = false,
                         fields: Option[Seq[String]] = None,
                         upsert: Boolean = false) extends Command {

  override val command: BsonDocument = {
    ("findAndModify" := collectionName) ~
      ("query" := query) ~
      ("sort" := sort) ~
      (removeOrUpdate match {
        case Left(remove) => "remove" := remove
        case Right(update) => "update" := update
      }) ~
      ("new" := returnNew) ~
      fields.map(fields => "fields" := $document(fields.map(_ := 1): _*)) ~
      ("upsert" := upsert)
  }
}
