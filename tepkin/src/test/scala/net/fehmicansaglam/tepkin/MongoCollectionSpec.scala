package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.tepkin.protocol.command.Index
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, OptionValues}

import scala.collection.immutable.Iterable
import scala.concurrent.Await
import scala.concurrent.duration._

class MongoCollectionSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter {

  override implicit val patienceConfig = PatienceConfig(timeout = 30.seconds, interval = 1.seconds)

  val client = MongoClient(Set(new InetSocketAddress("localhost", 27017)))
  val db = client("tepkin")
  val collection = db("mongo_collection_spec")

  import client.ec

  implicit val timeout: Timeout = 30.seconds

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

  it should "insert and find 10 documents" in {
    implicit val mat = ActorFlowMaterializer()(client.context)

    val documents = (1 to 10).map(i => $document("name" := s"fehmi$i"))

    val result = for {
      insertResult <- collection.insert(documents)
      source <- collection.find(BsonDocument.empty)
      count <- source.map(_.size).runFold(0) { (total, size) =>
        total + size
      }
    } yield count

    whenReady(result) { count =>
      count shouldBe 10
    }
  }

  it should "insert and find 1000 documents" in {
    implicit val mat = ActorFlowMaterializer()(client.context)

    val documents = (1 to 1000).map(i => $document("name" := s"fehmi$i"))

    val result = for {
      insertResult <- collection.insert(documents)
      source <- collection.find(BsonDocument.empty)
      count <- source.map(_.size).runFold(0) { (total, size) =>
        total + size
      }
    } yield count

    whenReady(result) { count =>
      count shouldBe 1000
    }
  }

  it should "insert and find 100000 documents" in {
    implicit val mat = ActorFlowMaterializer()(client.context)

    val documents: Source[List[BsonDocument], Unit] = Source {
      Iterable.tabulate(100) { _ =>
        (1 to 1000).map(i => $document("name" := s"fehmi$i")).toList
      }
    }

    val result = for {
      insertResult <- collection.insertFromSource(documents).runForeach(_ => ())
      source <- collection.find(BsonDocument.empty)
      count <- source.map(_.size).runFold(0) { (total, size) =>
        total + size
      }
    } yield count

    whenReady(result) { count =>
      count shouldBe 100000
    }
  }

  it should "update" in {
    val document = ("name" := "fehmi") ~ ("surname" := "saglam")

    val result = for {
      insert <- collection.insert(Seq(document))
      update <- collection.update(
        query = ("name" := "fehmi"),
        update = $set("name" := "fehmi can")
      )
    } yield update

    whenReady(result) { update =>
      update.ok shouldBe true
      update.n shouldBe 1
      update.nModified shouldBe 1
      update.upserted shouldBe 'empty
      update.writeErrors shouldBe 'empty
      update.writeConcernError shouldBe 'empty
    }
  }

  it should "create indexes" in {
    val result = for {
      create <- collection.createIndexes(Index(name = "name_surname", key = ("name" := 1) ~ ("surname" := 1)))
      list <- collection.getIndexes()
    } yield (create, list)

    whenReady(result) { case (create, list) =>
      list.exists(index => index.name == "name_surname") shouldBe true
    }
  }
}
