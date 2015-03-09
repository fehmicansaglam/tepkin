package net.fehmicansaglam.tepkin

import java.io.{File, FileInputStream}
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.security.MessageDigest

import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.element.BinarySubtype.Generic
import net.fehmicansaglam.bson.element.BsonObjectId
import net.fehmicansaglam.bson.util.Converters
import net.fehmicansaglam.tepkin.GridFs.Chunk
import net.fehmicansaglam.tepkin.protocol.command.Index
import org.joda.time.DateTime

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext

class GridFs(db: MongoDatabase, prefix: String = "fs") {
  val files = db(s"$prefix.files")
  val chunks = db(s"$prefix.chunks")

  /**
   * @return md5sum of the file
   */
  private def readFile(channel: FileChannel, consumer: (Int, ByteBuffer) => Unit): String = {
    val buffer = ByteBuffer.allocateDirect(255 * 1024)
    val md = MessageDigest.getInstance("MD5")

    @tailrec def readFile0(n: Int): Unit = {
      val len = channel.read(buffer)
      if (len > 0) {
        buffer.flip()
        md.update(buffer)
        consumer(n, buffer)
        buffer.clear()
        readFile0(n + 1)
      }
    }

    readFile0(0)
    Converters.hex2Str(md.digest())
  }

  def put(file: File)(implicit ec: ExecutionContext, timeout: Timeout): BsonDocument = {
    val channel = new FileInputStream(file).getChannel

    val fileId = BsonObjectId.generate
    val document = {
      ("_id" := fileId) ~
        ("length" := file.length()) ~
        ("chunkSize" := 255 * 1024) ~
        ("uploadDate" := DateTime.now()) ~
        ("filename" := file.getName)
    }

    val md5 = readFile(channel, (n, buffer) => {
      val array = new Array[Byte](buffer.remaining())
      buffer.get(array)
      val chunk = Chunk(fileId = fileId, n = n, data = BsonValueBinary(array, Generic))
      chunks.insert(chunk.toDoc)
      ()
    })

    files.insert(document ~ ("md5" := md5))

    chunks.createIndexes(Index(key = ("files_id" := 1) ~ ("n" := 1), name = "files_id_n"))
    document
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

}
