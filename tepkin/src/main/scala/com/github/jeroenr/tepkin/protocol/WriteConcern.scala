package com.github.jeroenr.tepkin.protocol

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.BsonDocument

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
