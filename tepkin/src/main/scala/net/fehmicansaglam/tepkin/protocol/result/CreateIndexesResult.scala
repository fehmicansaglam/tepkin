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
      ok = document.get[BsonNumber]("ok").map(_.toInt).get == 1,
      createdCollectionAutomatically = document.getAs[Boolean]("createdCollectionAutomatically").get,
      numIndexesBefore = document.getAs[Int]("numIndexesBefore").get,
      numIndexesAfter = document.getAs[Int]("numIndexesAfter").get,
      note = document.getAs[String]("note"),
      errmsg = document.getAs[String]("errmsg"),
      code = document.getAs[Int]("code")
    )
  }
}
