package com.github.jeroenr.tepkin

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.tepkin.TepkinMessage.WhatsYourVersion
import com.github.jeroenr.tepkin.protocol.MongoWireVersion
import com.github.jeroenr.tepkin.protocol.command.{Command, Create, ListCollections}
import com.github.jeroenr.tepkin.protocol.message.Reply

import scala.concurrent.{ExecutionContext, Future}

class MongoDatabase(pool: ActorRef, databaseName: String) {

  def apply(collectionName: String): MongoCollection = {
    require(collectionName != null && collectionName.getBytes("UTF-8").size < 123,
      "Collection name must be shorter than 123 bytes")
    new MongoCollection(databaseName, collectionName, pool)
  }

  def collection(collectionName: String): MongoCollection = apply(collectionName)

  def gridFs(prefix: String = "fs"): GridFs = {
    new GridFs(this, prefix)
  }

  /**
   * Creates a new collection explicitly. Because MongoDB creates a collection implicitly when the collection is first
   * referenced in a command, this method is used primarily for creating new capped collections. This is also used to
   * pre-allocate space for an ordinary collection.
   *
   * @param name The name of the collection to create.
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
   *                  collections with workloads that are insert-only or in-place updates (such as incrementing
   *                  counters). Defaults to false.
   * @param storageEngine Optional. Available for the WiredTiger storage engine only.
   */
  def createCollection(name: String,
                       capped: Option[Boolean] = None,
                       autoIndexId: Option[Boolean] = None,
                       size: Option[Int] = None,
                       max: Option[Int] = None,
                       usePowerOf2Sizes: Boolean = true,
                       noPadding: Boolean = false,
                       storageEngine: Option[BsonDocument] = None)
                      (implicit ec: ExecutionContext, timeout: Timeout): Future[BsonDocument] = {
    (pool ? Create(
      databaseName,
      name,
      capped,
      autoIndexId,
      size,
      max,
      usePowerOf2Sizes,
      noPadding,
      storageEngine)).mapTo[Reply].map(_.documents.head)
  }

  def listCollections(filter: Option[BsonDocument] = None, batchMultiplier: Int = 1000)
                     (implicit ec: ExecutionContext, timeout: Timeout): Future[Source[List[BsonDocument], ActorRef]] = {
    (pool ? WhatsYourVersion).mapTo[Int].map { maxWireVersion =>
      if (maxWireVersion == MongoWireVersion.v30) {
        val message = ListCollections(databaseName, filter)
        val extractor = { reply: Reply =>
          val cursor = reply.documents(0).getAs[BsonDocument]("cursor").get
          val cursorID = cursor.getAs[Long]("id").get
          val ns = cursor.getAs[String]("ns").get
          val initial = cursor.getAsList[BsonDocument]("firstBatch").get
          (ns, cursorID, initial)
        }

        Source.actorPublisher(MongoCursor.props(pool, message, extractor, batchMultiplier, timeout))
      } else {
        apply("system.namespaces").find(BsonDocument.empty)
      }
    }
  }

  def runCommand(document: BsonDocument)(implicit ec: ExecutionContext, timeout: Timeout): Future[Reply] = {
    val command = new Command {

      override def command: BsonDocument = document

      override def databaseName: String = MongoDatabase.this.databaseName
    }

    (pool ? command).mapTo[Reply]
  }

}

