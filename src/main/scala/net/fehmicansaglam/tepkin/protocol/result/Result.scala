package net.fehmicansaglam.tepkin.protocol.result

import net.fehmicansaglam.tepkin.bson.BsonDocument

sealed trait Result

case class WriteError(code: Int, errmsg: String)

object WriteError {
  def apply(document: BsonDocument): WriteError = {
    WriteError(
      document.getAs[Int]("code").get,
      document.getAs[String]("errmsg").get)
  }
}

case class WriteConcernError(code: Int, errInfo: BsonDocument, errmsg: String)

object WriteConcernError {
  def apply(document: BsonDocument): WriteConcernError = {
    WriteConcernError(
      document.getAs[Int]("code").get,
      document.getAs[BsonDocument]("errInfo").get,
      document.getAs[String]("errmsg").get)
  }
}

trait WriteResult extends Result {
  def ok: Boolean

  def n: Int

  def writeErrors: Option[List[WriteError]]

  def writeConcernError: Option[WriteConcernError]
}


case class CountResult(missing: Option[Boolean] = None, n: Long, ok: Boolean) extends Result

case class DeleteResult(n: Option[Int], code: Option[Int], errmsg: Option[String], ok: Boolean) extends Result

case class InsertResult(n: Int, ok: Boolean) extends Result

case class UpdateResult(ok: Boolean,
                        n: Int,
                        nModified: Int,
                        upserted: Option[List[BsonDocument]],
                        writeErrors: Option[List[WriteError]],
                        writeConcernError: Option[WriteConcernError]) extends WriteResult
