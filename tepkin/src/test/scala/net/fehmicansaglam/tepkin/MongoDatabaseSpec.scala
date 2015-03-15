package net.fehmicansaglam.tepkin

import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, OptionValues}

import scala.concurrent.duration._

class MongoDatabaseSpec
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


  "A MongoDatabase" should "list collections" in {
    implicit val mat = ActorFlowMaterializer()(client.context)

    val result = for {
      source <- db.listCollections()
      collections <- source.runFold(List.empty[BsonDocument])(_ ++ _)
    } yield collections

    whenReady(result) { collections =>
      Logger.debug(s"$collections")
    }
  }

}
