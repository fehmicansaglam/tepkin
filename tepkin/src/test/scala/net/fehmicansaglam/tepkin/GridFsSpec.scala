package net.fehmicansaglam.tepkin

import java.io.FileOutputStream

import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, OptionValues}

import scala.concurrent.Future
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

  import client.ec

  implicit val timeout: Timeout = 30.seconds

  "A GridFs" should "put file" in {
    implicit val mat = ActorFlowMaterializer()(client.context)
    val fs = db.gridFs()
    //    db.gridFs().put(new File("/Users/fehmicansaglam/Documents/git.pdf"))

    val out = new FileOutputStream("./git2.pdf")

    val result = fs.findOne("filename" := "git.pdf").flatMap {
      case Some(file) =>
        val id = file.get[BsonValueObjectId]("_id").get
        fs.get(id).flatMap { source =>
          source.runForeach(_.foreach(chunk => out.write(chunk.data.value)))
        }

      case None => Future.successful(())
    }

    whenReady(result) { _ =>
      out.close()
    }
  }

}
