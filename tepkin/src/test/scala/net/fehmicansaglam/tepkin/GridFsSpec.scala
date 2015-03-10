package net.fehmicansaglam.tepkin

import java.io.File

import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, OptionValues}

import scala.concurrent.duration._

class GridFsSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter {

  override implicit val patienceConfig = PatienceConfig(timeout = 30.seconds, interval = 1.seconds)

  val client = MongoClient("mongodb://localhost")
  val db = client("tepkin")
  val fs = db.gridFs()

  import client.ec

  implicit val timeout: Timeout = 30.seconds

  "A GridFs" should "put and delete file" in {
    implicit val mat = ActorFlowMaterializer()(client.context)

    val result = for {
      put <- fs.put(new File(getClass.getResource("/sample.pdf").getPath))
      file <- fs.findOne("filename" := "sample.pdf")
      delete <- fs.deleteOne("filename" := "sample.pdf")
    } yield (put, file, delete)

    whenReady(result) { case (put, file, delete) =>
      file shouldBe 'defined
      file.get.get[BsonValueObjectId]("_id") shouldBe put.get[BsonValueObjectId]("_id")
      delete.n shouldBe Some(1)
    }
  }

}
