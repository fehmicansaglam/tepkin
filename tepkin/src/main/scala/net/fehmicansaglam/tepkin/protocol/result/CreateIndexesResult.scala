package net.fehmicansaglam.tepkin.protocol.result

import net.fehmicansaglam.bson.{BsonDocument, BsonNumber}

case class CreateIndexesResult(ok: Boolean,
                               createdCollectionAutomatically: Boolean,
                               numIndexesBefore: Int,
                               numIndexesAfter: Int,
                               note: Option[String] = None,
                               errmsg: Option[String] = None,
                               code: Option[Int] = None)

object CreateIndexesResult {
  def apply(document: BsonDocument): CreateIndexesResult = {
    CreateIndexesResult(
      ok = document.get[BsonNumber]("ok").map(_.toInt).getOrElse(0) == 1,
      createdCollectionAutomatically = document.getAs[Boolean]("createdCollectionAutomatically").getOrElse(false),
      numIndexesBefore = document.getAs[Int]("numIndexesBefore").getOrElse(0),
      numIndexesAfter = document.getAs[Int]("numIndexesAfter").getOrElse(0),
      note = document.getAs[String]("note"),
      errmsg = document.getAs[String]("errmsg"),
      code = document.getAs[Int]("code")
    )
  }
}
