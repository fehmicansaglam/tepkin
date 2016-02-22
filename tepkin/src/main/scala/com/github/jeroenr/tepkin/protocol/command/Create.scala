package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}
import com.github.jeroenr.bson.Implicits._

/**
 * Explicitly creates a collection.
 *
 * @param collectionName The name of the new collection.
 * @param capped Optional. To create a capped collection. specify true. If you specify true, you must also set a
 *               maximum size in the size field.
 * @param autoIndexId Optional. Specify false to disable the automatic creation of an index on the _id field.
 * @param size Optional. The maximum size for the capped collection. Once a capped collection reaches its maximum size,
 *             MongoDB overwrites older old documents with new documents. The size field is required for capped
 *             collections.
 * @param max Optional. The maximum number of documents to keep in the capped collection. The size limit takes
 *            precedence over this limit. If a capped collection reaches its maximum size before it reaches the maximum
 *            number of documents, MongoDB removes old documents. If you use this limit, ensure that the size limit is
 *            sufficient to contain the documents limit.
 * @param usePowerOf2Sizes Optional. Available for the MMAPv1 storage engine only.
 *                         Deprecated since version 3.0: For the MMAPv1 storage engine, all collections use the power
 *                         of 2 sizes allocation unless the noPadding option is true. The usePowerOf2Sizes option does
 *                         not affect the allocation strategy. Defaults to true.
 * @param noPadding Optional. Available for the MMAPv1 storage engine only.
 *                  New in version 3.0: noPadding flag disables the power of 2 sizes allocation for the collection.
 *                  With noPadding flag set to true, the allocation strategy does not include additional space to
 *                  accommodate document growth, as such, document growth will result in new allocation. Use for
 *                  collections with workloads that are insert-only or in-place updates (such as incrementing counters).
 *                  Defaults to false.
 * @param storageEngine Optional. Available for the WiredTiger storage engine only.
 */
case class Create(databaseName: String,
                  collectionName: String,
                  capped: Option[Boolean] = None,
                  autoIndexId: Option[Boolean] = None,
                  size: Option[Int] = None,
                  max: Option[Int] = None,
                  usePowerOf2Sizes: Boolean = true,
                  noPadding: Boolean = false,
                  storageEngine: Option[BsonDocument] = None) extends Command {
  override def command: BsonDocument = {
    ("create" := collectionName) ~
      ("capped" := capped) ~
      ("autoIndexId" := autoIndexId) ~
      ("size" := size) ~
      ("max" := max) ~
      ("flags" := {
        (usePowerOf2Sizes, noPadding) match {
          case (false, false) => 0
          case (true, false) => 1
          case (false, true) => 2
          case (true, true) => 3
        }
      }) ~
      ("storageEngine" := storageEngine)
  }
}
