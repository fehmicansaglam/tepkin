package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument}

/**
 * The update command modifies documents in a collection. A single update command can contain multiple update
 * statements.
 *
 * @param collectionName The name of the target collection.
 * @param updates An array of one or more update statements to perform in the named collection.
 * @param ordered Optional. If true, then when an update statement fails, return without performing the remaining
 *                update statements. If false, then when an update fails, continue with the remaining update statements,
 *                if any. Defaults to true.
 * @param writeConcern Optional. A document expressing the write concern of the update command. Omit to use the default
 *                     write concern.
 */
case class Update(databaseName: String,
                  collectionName: String,
                  updates: Seq[UpdateElement],
                  ordered: Option[Boolean] = None,
                  writeConcern: Option[BsonDocument] = None) extends Command {
  override val command: BsonDocument = {
    ("update" := collectionName) ~
      ("updates" := $array(updates.map(_.asBsonDocument): _*)) ~
      ("ordered" := ordered) ~
      ("writeConcern" := writeConcern)
  }
}

/**
 * Update statement.
 *
 * @param q The query that matches documents to update.
 * @param u The modifications to apply.
 * @param upsert Optional. If true, perform an insert if no documents match the query. If both upsert and multi are true
 *               and no documents match the query, the update operation inserts only a single document.
 * @param multi Optional. If true, updates all documents that meet the query criteria. If false, limit the update to one
 *              document that meet the query criteria. Defaults to false.
 */
case class UpdateElement(q: BsonDocument,
                         u: BsonDocument,
                         upsert: Option[Boolean] = None,
                         multi: Option[Boolean] = None) {
  val asBsonDocument: BsonDocument = {
    ("q" := q) ~
      ("u" := u) ~
      ("upsert" := upsert) ~
      ("multi" := multi)
  }
}
