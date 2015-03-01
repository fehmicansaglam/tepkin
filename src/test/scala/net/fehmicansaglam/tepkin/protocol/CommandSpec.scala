package net.fehmicansaglam.tepkin.protocol

import java.nio.ByteOrder

import akka.util.ByteString
import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._
import net.fehmicansaglam.tepkin.protocol.command._
import org.scalatest.{FlatSpec, Matchers}

class CommandSpec extends FlatSpec with Matchers {

  implicit val byteOrder = ByteOrder.LITTLE_ENDIAN
  val databaseName = "tepkin"
  val collectionName = "command_spec"

  "Command" should "construct correct Count" in {
    val query = "age" := 18

    val actual = Count(databaseName, collectionName, Some(query))

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0)
        .putBytes((databaseName + ".$cmd").getBytes("utf-8"))
        .putByte(0)
        .putInt(0) // numberToSkip
        .putInt(1) // numberToReturn
        .append((("count" := collectionName) ~ ("query" := query)).encode())
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2004)
        .append(body)
        .result()
    }

    actual.encode() should be(expected)
  }

  it should "construct correct Delete" in {
    val deletes: Seq[DeleteElement] = Seq(DeleteElement(q = "age" := 18))

    val actual = Delete(databaseName, collectionName, deletes)

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0)
        .putBytes((databaseName + ".$cmd").getBytes("utf-8"))
        .putByte(0)
        .putInt(0) // numberToSkip
        .putInt(1) // numberToReturn
        .append((("delete" := collectionName) ~ ("deletes" := $array(deletes.map(_.asBsonDocument): _*))).encode())
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2004)
        .append(body)
        .result()
    }

    actual.encode() should be(expected)
  }

  it should "construct correct FindAndModify" in {
    val query: Option[BsonDocument] = Some("age" := 18)

    val actual = FindAndModify(databaseName, collectionName, query, removeOrUpdate = Left(true))

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0)
        .putBytes((databaseName + ".$cmd").getBytes("utf-8"))
        .putByte(0)
        .putInt(0) // numberToSkip
        .putInt(1) // numberToReturn
        .append((
        ("findAndModify" := collectionName) ~
          ("query" := query) ~
          ("remove" := true) ~
          ("new" := false) ~
          ("upsert" := false))
        .encode())
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2004)
        .append(body)
        .result()
    }

    actual.encode() should be(expected)
  }

  it should "construct correct GetLastError" in {
    val actual = GetLastError(databaseName)

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0)
        .putBytes((databaseName + ".$cmd").getBytes("utf-8"))
        .putByte(0)
        .putInt(0) // numberToSkip
        .putInt(1) // numberToReturn
        .append($document("getLastError" := 1).encode())
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2004)
        .append(body)
        .result()
    }

    actual.encode() should be(expected)
  }

  it should "construct correct Insert" in {
    val documents: Seq[BsonDocument] = Seq("age" := 18)

    val actual = Insert(databaseName, collectionName, documents)

    val expected = {
      val body = ByteString.newBuilder
        .putInt(0)
        .putBytes((databaseName + ".$cmd").getBytes("utf-8"))
        .putByte(0)
        .putInt(0) // numberToSkip
        .putInt(1) // numberToReturn
        .append((("insert" := collectionName) ~ ("documents" := $array(documents: _*)) ~ ("ordered" := true)).encode())
        .result()

      ByteString.newBuilder
        .putInt(body.size + 16)
        .putInt(actual.requestID)
        .putInt(0)
        .putInt(2004)
        .append(body)
        .result()
    }

    actual.encode() should be(expected)
  }

}
