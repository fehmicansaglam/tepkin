package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument}

/**
 * The delete command removes documents from a collection. A single delete command can contain multiple delete
 * specifications. The command cannot operate on capped collections.
 *
 * @param collectionName The name of the target collection.
 * @param deletes An array of one or more delete statements to perform in the named collection.
 * @param ordered Optional. If true, then when a delete statement fails, return without performing the remaining delete
 *                statements. If false, then when a delete statement fails, continue with the remaining delete
 *                statements, if any. Defaults to true.
 * @param writeConcern Optional. A document expressing the write concern of the delete command. Omit to use the default
 *                     write concern.
 */
case class Delete(databaseName: String,
                  collectionName: String,
                  deletes: Seq[DeleteElement],
                  ordered: Option[Boolean] = None,
                  writeConcern: Option[BsonDocument] = None) extends Command {
  override val command: BsonDocument = {
    ("delete" := collectionName) ~
      ("deletes" := $array(deletes.map(_.asBsonDocument): _*)) ~
      ("ordered" := ordered) ~
      ("writeConcern" := writeConcern)
  }
}

/**
 * Delete statement.
 *
 * @param q The query that matches documents to delete.
 * @param limit The number of matching documents to delete. Specify either a 0 to delete all matching documents
 *              or 1 to delete a single document.
 */
case class DeleteElement(q: BsonDocument, limit: Int) {
  val asBsonDocument: BsonDocument = ("q" := q) ~ ("limit" := limit)
}
