package com.github.jeroenr.tepkin

import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.github.jeroenr.bson.BsonDocument
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._

@DoNotDiscover
class MongoDatabaseSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter
  with BeforeAndAfterAll {

  override implicit val patienceConfig = PatienceConfig(timeout = 30.seconds, interval = 1.seconds)

  var client: MongoClient = _
  var db: MongoDatabase = _

  override protected def beforeAll() = {
    client = MongoClient("mongodb://localhost:12345")
    db = client("tepkin")
  }

  implicit val timeout: Timeout = 30.seconds

  override protected def afterAll() = client.shutdown()

  "A MongoDatabase" should "list collections" in {
    implicit val context = client.context
    implicit val ec = client.ec
    implicit val mat = ActorMaterializer()

    val result = for {
      source <- db.listCollections()
      collections <- source.runFold(List.empty[BsonDocument])(_ ++ _)
    } yield collections

    whenReady(result) { collections =>
      Logger.debug(s"$collections")
    }
  }

}
