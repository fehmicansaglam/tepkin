package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

/**
 * Performs aggregation operation using the aggregation pipeline. The pipeline allows users to process data from a
 * collection with a sequence of stage-based manipulations.
 *
 * @param pipeline An array of aggregation pipeline stages that process and transform the document stream as part of
 *                 the aggregation pipeline.
 * @param explain Optional. Specifies to return the information on the processing of the pipeline.
 * @param allowDiskUse Optional. Enables writing to temporary files. When set to true, aggregation stages can write
 *                     data to the _tmp subdirectory in the dbPath directory.
 * @param cursor Optional. Specify a document that contains options that control the creation of the cursor object.
 */
case class Aggregate(databaseName: String,
                     collectionName: String,
                     pipeline: List[BsonDocument],
                     explain: Option[Boolean],
                     allowDiskUse: Option[Boolean],
                     cursor: Option[BsonDocument]) extends Command {
  override val command: BsonDocument = {
    ("aggregate" := collectionName) ~
      ("pipeline" := $array(pipeline: _*)) ~
      ("explain" := explain) ~
      ("allowDiskUse" := allowDiskUse) ~
      ("cursor" := cursor)
  }
}
