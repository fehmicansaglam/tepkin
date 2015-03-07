package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.{BsonDocument, BsonValue, BsonDsl, Implicits}
import BsonDsl._
import Implicits._

case class GetLastError(databaseName: String,
                        j: Option[Boolean] = None,
                        w: Option[BsonValue] = None,
                        fsync: Option[Boolean] = None,
                        wtimeout: Option[Int] = None) extends Command {
  override val command: BsonDocument = {
    ("getLastError" := 1) ~
      ("j" := j) ~
      ("w" := w) ~
      ("fsync" := fsync) ~
      ("wtimeout" := wtimeout)
  }
}
