package com.github.jeroenr.tepkin.protocol.result

import com.github.jeroenr.bson.BsonDocument

protected[result] trait Result

case class CountResult(missing: Option[Boolean] = None, n: Int, ok: Boolean) extends Result {
  /** Alias for n **/
  def count: Int = n
}

case class DeleteResult(ok: Boolean,
                        n: Int,
                        operationError: Option[OperationError] = None,
                        writeErrors: Option[List[WriteError]] = None,
                        writeConcernError: Option[WriteConcernError] = None) extends WriteResult

case class InsertResult(ok: Boolean,
                        n: Int,
                        operationError: Option[OperationError] = None,
                        writeErrors: Option[List[WriteError]] = None,
                        writeConcernError: Option[WriteConcernError] = None) extends WriteResult

case class UpdateResult(ok: Boolean,
                        n: Int,
                        nModified: Int,
                        upserted: Option[List[BsonDocument]],
                        operationError: Option[OperationError] = None,
                        writeErrors: Option[List[WriteError]] = None,
                        writeConcernError: Option[WriteConcernError] = None) extends WriteResult

