package net.fehmicansaglam.tepkin.protocol.message

import akka.util.ByteString
import net.fehmicansaglam.bson.BsonDocument

/**
 * The OP_QUERY message is used to query the database for documents in a collection.
 * The format of the OP_QUERY message is:
 *
 * {{{
 * struct OP_QUERY {
 *     MsgHeader header;                 // standard message header
 *     int32     flags;                  // bit vector of query options.
 *     cstring   fullCollectionName;     // "dbname.collectionname"
 *     int32     numberToSkip;           // number of documents to skip
 *     int32     numberToReturn;         // number of documents to return in the first OP_REPLY batch
 *     document  query;                  // query object.
 *   [ document  returnFieldsSelector; ] // Optional. Selector indicating the fields to return.
 * }
 * }}}
 */
case class QueryMessage(fullCollectionName: String,
                        query: BsonDocument,
                        fields: Option[BsonDocument] = None,
                        flags: Int = 0,
                        numberToSkip: Int = 0,
                        numberToReturn: Int = 0) extends Message {

  override val responseTo: Int = 0

  override val opCode: Int = 2004

  override def encodeBody: ByteString = {
    val builder = ByteString.newBuilder
      .putInt(flags)
      .putBytes(fullCollectionName.getBytes("utf-8"))
      .putByte(0)
      .putInt(numberToSkip)
      .putInt(numberToReturn)
      .append(query.encode)

    fields.foreach(fields => builder.append(fields.encode))

    builder.result()
  }

}


object QueryOptions {
  /**
   * Tailable means cursor is not closed when the last data is retrieved. Rather, the cursor marks the final object’s
   * position. You can resume using the cursor later, from where it was located, if more data were received. Like any
   * “latent cursor”, the cursor may become invalid at some point (CursorNotFound) – for example if the final object it
   * references were deleted.
   */
  val TailableCursor: Int = 2

  /**
   * Allow query of replica slave. Normally these return an error except for namespace “local”.
   */
  val SlaveOk: Int = 4

  /**
   * The server normally times out idle cursors after an inactivity period (10 minutes) to prevent excess memory use.
   * Set this option to prevent that.
   */
  val NoCursorTimeout: Int = 16

  /**
   * Use with TailableCursor. If we are at the end of the data, block for a while rather than returning no data. After
   * a timeout period, we do return as normal.
   */
  val AwaitData: Int = 32

  /**
   * Stream the data down full blast in multiple “more” packages, on the assumption that the client will fully read all
   * data queried. Faster when you are pulling a lot of data and know you want to pull it all down. Note: the client is
   * not allowed to not read all the data unless it closes the connection.
   */
  val Exhaust: Int = 64

  /**
   * Get partial results from a mongos if some shards are down (instead of throwing an error)
   */
  val Partial: Int = 128

}
