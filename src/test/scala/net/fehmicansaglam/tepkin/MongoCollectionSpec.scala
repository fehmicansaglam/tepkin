package net.fehmicansaglam.tepkin

import akka.util.Timeout
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, OptionValues}

import scala.concurrent.Await
import scala.concurrent.duration._

class MongoCollectionSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter {

  override implicit val patienceConfig = PatienceConfig(timeout = 5.seconds, interval = 1.seconds)

  val client = MongoClient("localhost", 27017)
  val collection = client("tepkin", "mongo_collection_spec")

  import client.ec

  implicit val timeout: Timeout = 5.seconds

  before {
    Await.ready(collection.drop(), 5.seconds)
  }

  after {
    Await.ready(collection.drop(), 5.seconds)
  }

  "A MongoCollection" should "findAndUpdate" in {
    val document = ("name" := "fehmi") ~ ("surname" := "saglam")
    val result = for {
      insert <- collection.insert(Seq(document))
      newDocument <- collection.findAndUpdate(
        query = Some("name" := "fehmi"),
        update = $set("name" := "fehmi can"),
        returnNew = true
      )
    } yield newDocument

    whenReady(result) { newDocument =>
      newDocument.flatMap(_.getAs("name")).value shouldBe "fehmi can"
    }
  }
}
