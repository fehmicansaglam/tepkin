package com.github.jeroenr.tepkin

import java.io._
import java.security.MessageDigest

import akka.actor.ActorRef
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.{ByteString, Timeout}
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._
import com.github.jeroenr.bson.element.BinarySubtype.Generic
import com.github.jeroenr.bson.element.{BinarySubtype, BsonObjectId}
import com.github.jeroenr.bson.util.Converters
import com.github.jeroenr.bson.{BsonDocument, BsonDsl, Implicits}
import com.github.jeroenr.tepkin.GridFs.Chunk
import com.github.jeroenr.tepkin.protocol.command.Index
import com.github.jeroenr.tepkin.protocol.result.DeleteResult
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

class GridFs(db: MongoDatabase, prefix: String = "fs") {
  val chunkSize = 255 * 1024
  val files = db(s"$prefix.files")
  val chunks = db(s"$prefix.chunks")

  def findOne(query: BsonDocument)(implicit ec: ExecutionContext, timeout: Timeout): Future[Option[BsonDocument]] = {
    files.findOne(query)
  }

  /**
   * Puts the given file into GridFS.
   */
  def put(file: File)(implicit mat: Materializer, ec: ExecutionContext, timeout: Timeout): Future[BsonDocument] = {
    val fileId = BsonObjectId.generate
    val zero = (0, MessageDigest.getInstance("MD5"))

    FileIO.fromPath(file.toPath, chunkSize).runFold(zero) { case ((n, md), data) =>
      md.update(data.asByteBuffer)
      val chunk = Chunk(fileId = fileId, n = n, data = BsonValueBinary(data, Generic))
      chunks.insert(chunk.toDoc)
      (n + 1, md)
    }.flatMap { case (_, md) =>
      chunks.createIndexes(Index(key = ("files_id" := 1) ~ ("n" := 1), name = "files_id_n"))
      val md5 = Converters.hex2Str(md.digest())
      val document = {
        ("_id" := fileId) ~
          ("length" := file.length()) ~
          ("chunkSize" := chunkSize) ~
          ("uploadDate" := DateTime.now()) ~
          ("filename" := file.getName) ~
          ("md5" := md5)
      }
      files.insert(document).map(_ => document)
    }
  }

  /**
   * Puts the data from InputStream into GridFS.
   *
   * @param name name of the file
   */
  def put(name: String, input: InputStream)(implicit ec: ExecutionContext, timeout: Timeout): Future[BsonDocument] = {
    val buffer = new Array[Byte](chunkSize)
    val reader = new BufferedInputStream(input, chunkSize)
    val md = MessageDigest.getInstance("MD5")
    val fileId = BsonObjectId.generate

    val length = Iterator.continually(reader.read(buffer)).takeWhile(_ != -1).zipWithIndex.foldLeft(0) {
      case (total, (length, index)) =>
        md.update(buffer, 0, length)
        val chunk = Chunk(
          fileId = fileId,
          n = index,
          data = BsonValueBinary(ByteString.fromArray(buffer, 0, length), Generic))
        chunks.insert(chunk.toDoc)
        total + length
    }

    reader.close()

    chunks.createIndexes(Index(key = ("files_id" := 1) ~ ("n" := 1), name = "files_id_n"))

    val document = {
      ("_id" := fileId) ~
        ("length" := length) ~
        ("chunkSize" := chunkSize) ~
        ("uploadDate" := DateTime.now()) ~
        ("filename" := name) ~
        ("md5" := Converters.hex2Str(md.digest()))
    }
    files.insert(document).map(_ => document)
  }

  /**
   * Gets the file as a Chunk stream.
   *
   * @param id _id of the file
   */
  def get(id: BsonValueObjectId)
         (implicit timeout: Timeout): Source[Chunk, ActorRef] = {
    chunks.find($query("files_id" := id) ~ $orderBy("n" := 1)).mapConcat(_.map(Chunk.apply))
  }

  def getOne(query: BsonDocument)
            (implicit ec: ExecutionContext, timeout: Timeout): Future[Option[Source[Chunk, ActorRef]]] = {
    findOne(query).map {
      case Some(file) =>
        val id = file.get[BsonValueObjectId]("_id").get
        Some(get(id))

      case None => None
    }
  }

  /**
   * Deletes the specified file from GridFS storage.
   *
   * @param id _id of the file
   */
  def delete(id: BsonValueObjectId)(implicit ec: ExecutionContext, timeout: Timeout): Future[DeleteResult] = {
    for {
      deleteChunks <- chunks.delete("files_id" := id)
      deleteFile <- files.delete("_id" := id)
    } yield deleteFile
  }

  /** Deletes at most one file from GridFS storage matching the given criteria. */
  def deleteOne(query: BsonDocument)(implicit ec: ExecutionContext, timeout: Timeout): Future[DeleteResult] = {
    findOne(query).flatMap {
      case Some(file) =>
        delete(file.get[BsonValueObjectId]("_id").get)
      case None =>
        Future.successful(DeleteResult(ok = true, n = 0))
    }
  }
}

object GridFs {

  case class Chunk(id: BsonValueObjectId = BsonObjectId.generate,
                   fileId: BsonValueObjectId,
                   n: Int,
                   data: BsonValueBinary) {
    val toDoc: BsonDocument = {
      ("_id" := id) ~
        ("files_id" := fileId) ~
        ("n" := n) ~
        ("data" := data)
    }
  }

  object Chunk {
    def apply(document: BsonDocument): Chunk = {
      Chunk(
        id = document.get[BsonValueObjectId]("_id").get,
        fileId = document.get[BsonValueObjectId]("files_id").get,
        n = document.getAs[Int]("n").get,
        data = document.get[BsonValueBinary]("data").get)
    }
  }

}
