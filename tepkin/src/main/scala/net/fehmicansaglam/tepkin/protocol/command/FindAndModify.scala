package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

/**
 * The findAndModify command modifies and returns a single document. By default, the returned document does not include
 * the modifications made on the update. To return the document with the modifications made on the update,
 * use the new option.
 *
 * @param collectionName The collection against which to run the command.
 * @param query Optional. The selection criteria for the modification. Although the query may match multiple documents,
 *              findAndModify will only select one document to modify.
 * @param sort Optional. Determines which document the operation modifies if the query selects multiple documents.
 *             findAndModify modifies the first document in the sort order specified by this argument.
 * @param removeOrUpdate Must specify either the remove or the update field. Remove removes the document specified
 *                       in the query field. Set this to true to remove the selected document. The default is false.
 *                       Update performs an update of the selected document. The update field employs the same update
 *                       operators or field: value specifications to modify the selected document.
 *
 * @param returnNew Optional. When true, returns the modified document rather than the original. The findAndModify
 *                  method ignores the new option for remove operations. The default is false.
 * @param fields Optional. A subset of fields to return. The fields document specifies an inclusion of a field with 1.
 * @param upsert Optional. Used in conjunction with the update field. When true, findAndModify creates a new document
 *               if no document matches the query, or if documents match the query, findAndModify performs an update.
 *               To avoid multiple upserts, ensure that the query fields are uniquely indexed. The default is false.
 */
case class FindAndModify(databaseName: String,
                         collectionName: String,
                         query: Option[BsonDocument] = None,
                         sort: Option[BsonDocument] = None,
                         removeOrUpdate: Either[Boolean, BsonDocument],
                         returnNew: Boolean = false,
                         fields: Option[Seq[String]] = None,
                         upsert: Boolean = false) extends Command {

  override val command: BsonDocument = {
    ("findAndModify" := collectionName) ~
      ("query" := query) ~
      ("sort" := sort) ~
      (removeOrUpdate match {
        case Left(remove) => "remove" := remove
        case Right(update) => "update" := update
      }) ~
      ("new" := returnNew) ~
      fields.map(fields => "fields" := $document(fields.map(_ := 1): _*)) ~
      ("upsert" := upsert)
  }
}
