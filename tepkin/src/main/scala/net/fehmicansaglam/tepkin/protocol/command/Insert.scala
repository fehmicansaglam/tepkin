package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

/**
 * The insert command inserts one or more documents and returns a document containing the status of all inserts.
 *
 * @param collectionName The name of the target collection.
 * @param documents An array of one or more documents to insert into the named collection.
 * @param ordered Optional. If true, then when an insert of a document fails, return without inserting any remaining
 *                documents listed in the inserts array. If false, then when an insert of a document fails,
 *                continue to insert the remaining documents. Defaults to true.
 * @param writeConcern Optional. A document that expresses the write concern of the insert command. Omit to use the
 *                     default write concern.
 */
case class Insert(databaseName: String,
                  collectionName: String,
                  documents: Seq[BsonDocument],
                  ordered: Option[Boolean] = None,
                  writeConcern: Option[BsonDocument] = None) extends Command {

  override val command: BsonDocument = {
    ("insert" := collectionName) ~
      ("documents" := $array(documents: _*)) ~
      ("ordered" := ordered) ~
      ("writeConcern" := writeConcern)
  }
}
