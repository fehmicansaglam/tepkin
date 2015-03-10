package net.fehmicansaglam.tepkin.protocol.result

import net.fehmicansaglam.bson.BsonDocument

protected[result] trait Result

case class CountResult(missing: Option[Boolean] = None, n: Int, ok: Boolean) extends Result

case class DeleteResult(n: Option[Int] = None,
                        code: Option[Int] = None,
                        errmsg: Option[String] = None,
                        ok: Boolean) extends Result

case class InsertResult(n: Int, ok: Boolean) extends Result

case class UpdateResult(ok: Boolean,
                        n: Int,
                        nModified: Int,
                        upserted: Option[List[BsonDocument]],
                        writeErrors: Option[List[WriteError]],
                        writeConcernError: Option[WriteConcernError]) extends WriteResult

