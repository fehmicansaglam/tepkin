package com.github.jeroenr.tepkin.protocol

import java.nio.ByteOrder

import akka.util.ByteString
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl}
import com.github.jeroenr.tepkin.protocol.message._
import org.scalatest.{FlatSpec, Matchers}

class MessageSpec extends FlatSpec with Matchers {

  implicit val byteOrder = ByteOrder.LITTLE_ENDIAN
  val fullCollectionName = "tepkin.message_spec"

  "Message" should "construct correct DeleteMessage" in {
    val selector: BsonDocument = "age" := 18

    val actual = DeleteMessage(fullCollectionName, selector)

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0) // ZERO
        .putBytes(fullCollectionName.getBytes("utf-8"))
        .putByte(0)
        .putInt(0)
        .append(selector.encode)
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2006)
        .append(body)
        .result()
    }

    actual.encode should be(expected)
  }

  it should "construct correct GetMoreMessage" in {
    val cursorID = 1L
    val numberToReturn = 0

    val actual = GetMoreMessage(fullCollectionName, cursorID, numberToReturn)

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0) // ZERO
        .putBytes(fullCollectionName.getBytes("utf-8"))
        .putByte(0)
        .putInt(numberToReturn)
        .putLong(cursorID)
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2005)
        .append(body)
        .result()
    }

    actual.encode should be(expected)
  }

  it should "construct correct InsertMessage" in {
    val document: BsonDocument = "age" := 18

    val actual = InsertMessage(fullCollectionName, Seq(document))

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0) // flags
        .putBytes(fullCollectionName.getBytes("utf-8"))
        .putByte(0)
        .append(document.encode)
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2002)
        .append(body)
        .result()
    }

    actual.encode should be(expected)
  }

  it should "construct correct KillCursorsMessage" in {
    val cursorIDs = Seq(1L, 2L)

    val actual = KillCursorsMessage(cursorIDs: _*)

    val expected = {
      val builder = ByteString.newBuilder
        .putInt(0) // ZERO
        .putInt(cursorIDs.size)

      cursorIDs.foreach(builder.putLong)

      val body = builder.result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2007)
        .append(body)
        .result()
    }

    actual.encode should be(expected)
  }

  it should "construct correct QueryMessage" in {
    val query: BsonDocument = "age" := 18
    val fields: BsonDocument = "age" := 1
    val numberToSkip = 0
    val numberToReturn = 0

    val actual = QueryMessage(fullCollectionName, query, Some(fields), numberToSkip, numberToReturn)

    val expected = {
      val builder = ByteString.newBuilder
        .putInt(0) //flags
        .putBytes(fullCollectionName.getBytes("utf-8"))
        .putByte(0)
        .putInt(numberToSkip)
        .putInt(numberToReturn)
        .append(query.encode)

      builder.append(fields.encode)

      val body = builder.result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2004)
        .append(body)
        .result()
    }

    actual.encode should be(expected)
  }

  it should "construct correct UpdateMessage" in {
    val selector: BsonDocument = "age" := 18
    val update: BsonDocument = $set("age" := 33)

    val actual = UpdateMessage(fullCollectionName, selector, update)

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0) // ZERO
        .putBytes(fullCollectionName.getBytes("utf-8"))
        .putByte(0)
        .putInt(0) // flags
        .append(selector.encode)
        .append(update.encode)
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2001)
        .append(body)
        .result()
    }

    actual.encode should be(expected)
  }

}
