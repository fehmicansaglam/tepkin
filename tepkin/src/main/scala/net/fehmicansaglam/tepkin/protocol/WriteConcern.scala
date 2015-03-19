package net.fehmicansaglam.tepkin.protocol

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

case class WriteConcern(w: Either[String, Int] = Right(1),
                        j: Boolean = false,
                        wtimeout: Option[Int] = None) {
  val toDoc: BsonDocument = {
    (w match {
      case Left(w) => "w" := w
      case Right(w) => "w" := w
    }) ~
      ("j" := j) ~
      ("wtimeout" := wtimeout)
  }
}
