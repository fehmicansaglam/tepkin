package net.fehmicansaglam.tepkin.protocol.result

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.protocol.exception.{WriteConcernException, WriteException}

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

  def hasWriteError: Boolean = writeErrors.exists(_.nonEmpty)

  def hasWriteConcernError: Boolean = writeConcernError.isDefined

  /**
   * Used to explicitly throw an exception when the result is not OK.
   */
  def convertErrorToException(): this.type = {
    if (!ok && !hasWriteError && !hasWriteConcernError) {
      throw new IllegalStateException("Result is not OK but there are no errors.")
    }

    if (hasWriteError) throw WriteException(writeErrors.get)
    if (hasWriteConcernError) throw WriteConcernException(writeConcernError.get)

    this
  }

}
