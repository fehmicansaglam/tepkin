package com.github.jeroenr.tepkin.protocol.message

import java.nio.{ByteBuffer, ByteOrder}

import akka.util.ByteString
import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.bson.reader.BsonDocumentReader

import scala.collection.mutable.ArrayBuffer

/**
 * The OP_REPLY message is sent by the database in response to an OP_QUERY or OP_GET_MORE message.
 * The format of an OP_REPLY message is:
 *
 * {{{
 * struct {
 *     MsgHeader header;         // standard message header
 *     int32     responseFlags;  // bit vector
 *     int64     cursorID;       // cursor id if client needs to do get more's
 *     int32     startingFrom;   // where in the cursor this reply is starting
 *     int32     numberReturned; // number of documents in the reply
 *     document* documents;      // documents
 * }
 * }}}
 */
case class Reply(responseTo: Int,
                 responseFlags: Int,
                 cursorID: Long,
                 startingFrom: Int,
                 numberReturned: Int,
                 documents: List[BsonDocument]) extends Message {

  override def opCode: Int = Reply.OP_CODE

  override def encodeBody: ByteString = {
    val builder = ByteString.newBuilder
      .putInt(responseFlags)
      .putLong(cursorID)
      .putInt(startingFrom)
      .putInt(numberReturned)

    documents.foreach(doc => builder.append(doc.encode))

    builder.result()
  }

  def queryFailed: Boolean = (responseFlags & ResponseFlags.QueryFailure) != 0

  def cursorNotFound: Boolean = (responseFlags & ResponseFlags.CursorNotFound) != 0
}

object Reply {

  val OP_CODE = 1

  def apply(buffer: ByteBuffer): Option[Reply] = {
    buffer.order(ByteOrder.LITTLE_ENDIAN)
    val length = buffer.getInt

    if (length != buffer.remaining() + 4) {
      None
    } else {
      buffer.getInt // requestID, unused for Reply
      val responseTo = buffer.getInt
      val opCode = buffer.getInt
      if (opCode == OP_CODE) {
        val responseFlags = buffer.getInt
        val cursorID = buffer.getLong
        val startingFrom = buffer.getInt
        val numberReturned = buffer.getInt

        val documents = ArrayBuffer[BsonDocument]()

        while (buffer.hasRemaining) {
          documents ++= BsonDocumentReader.read(buffer)
        }

        Some(Reply(responseTo, responseFlags, cursorID, startingFrom, numberReturned, documents.toList))
      } else {
        None
      }
    }
  }

}


object ResponseFlags {

  /**
   * Set when getMore is called but the cursor id is not valid at the server. Returned with zero results.
   */
  val CursorNotFound: Int = 1

  /**
   * Set when query failed. Results consist of one document containing an $$err field describing the failure.
   */
  val QueryFailure: Int = 2

  /**
   * Set when the server supports the AwaitData Query option. If it doesn’t, a client should sleep a little
   * between getMore’s of a Tailable cursor. Mongod version 1.6 supports AwaitData and thus always sets AwaitCapable.
   */
  val AwaitCapable: Int = 8
}
