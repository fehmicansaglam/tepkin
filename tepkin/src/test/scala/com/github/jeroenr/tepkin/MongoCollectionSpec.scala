package com.github.jeroenr.tepkin

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._
import com.github.jeroenr.bson.{BsonDocument, BsonDsl, Implicits}
import com.github.jeroenr.tepkin.protocol.command.Index
import com.github.jeroenr.tepkin.protocol.exception.WriteException
import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.collection.immutable.Iterable
import scala.concurrent.Await
import scala.concurrent.duration._

@DoNotDiscover
class MongoCollectionSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter
  with BeforeAndAfterAll {

  override implicit val patienceConfig = PatienceConfig(timeout = 30.seconds, interval = 1.seconds)

  var client: MongoClient = _
  var db: MongoDatabase = _
  var collection: MongoCollection = _

  override protected def beforeAll() = {
    client = MongoClient("mongodb://localhost:12345")
    db = client("tepkin")
    collection = db("mongo_collection_spec")
  }

  implicit val timeout: Timeout = 30.seconds

  before {
    implicit val context = client.context
    implicit val ec = client.ec
    Await.ready(collection.drop(), 5.seconds)
  }

  after {
    implicit val context = client.context
    implicit val ec = client.ec
    Await.ready(collection.drop(), 5.seconds)
  }

  override protected def afterAll() = client.shutdown()

  "A MongoCollection" should "findAndUpdate" in {
    implicit val context = client.context
    implicit val ec = client.ec
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
    implicit val context = client.context
    implicit val ec = client.ec
    implicit val mat = ActorMaterializer()

    val documents = (1 to 10).map(i => $document("name" := s"fehmi$i"))

    val result = for {
      insertResult <- collection.insert(documents)
      count <- collection.find(BsonDocument.empty).map(_.size).runFold(0) { (total, size) =>
        total + size
      }
    } yield count

    whenReady(result) { count =>
      count shouldBe 10
    }
  }

  it should "insert and find 1000 documents" in {
    implicit val context = client.context
    implicit val ec = client.ec
    implicit val mat = ActorMaterializer()

    val documents = (1 to 1000).map(i => $document("name" := s"fehmi$i"))

    val result = for {
      insertResult <- collection.insert(documents)
      count <- collection.find(BsonDocument.empty).map(_.size).runFold(0) { (total, size) =>
        total + size
      }
    } yield count

    whenReady(result) { count =>
      count shouldBe 1000
    }
  }

  it should "insert and find 100000 documents" in {
    implicit val context = client.context
    implicit val ec = client.ec
    implicit val mat = ActorMaterializer()

    val documents: Source[List[BsonDocument], akka.NotUsed] = Source {
      Iterable.tabulate(100) { _ =>
        (1 to 1000).map(i => $document("name" := s"fehmi$i")).toList
      }
    }

    val result = for {
      insertResult <- collection.insertFromSource(documents).runForeach(_ => ())
      count <- collection.find(BsonDocument.empty).map(_.size).runFold(0) { (total, size) =>
        total + size
      }
    } yield count

    whenReady(result) { count =>
      count shouldBe 100000
    }
  }

  it should "update" in {
    implicit val context = client.context
    implicit val ec = client.ec
    val document = ("name" := "fehmi") ~ ("surname" := "saglam")

    val result = for {
      insert <- collection.insert(Seq(document))
      update <- collection.update(
        query = "name" := "fehmi",
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

  it should "create indexes" ignore {
    implicit val context = client.context
    implicit val ec = client.ec
    val result = for {
      create <- collection.createIndexes(Index(name = "name_surname", key = ("name" := 1) ~ ("surname" := 1)))
      list <- collection.getIndexes()
    } yield (create, list)

    whenReady(result) { case (create, list) =>
      list.exists(index => index.name == "name_surname") shouldBe true
    }
  }

  it should "find distinct values" in {
    implicit val context = client.context
    implicit val ec = client.ec
    val documents: Seq[BsonDocument] = Seq("name" := "aa", "name" := "bb", "name" := "cc", "name" := "aa")

    val result = for {
      insert <- collection.insert(documents)
      count <- collection.count()
      distinct <- collection.distinct("name")
    } yield (count, distinct)

    whenReady(result) { case (count, distinct) =>
      count.n shouldBe 4
      distinct.values.collect {
        case BsonValueString(value) => value
      } shouldBe Seq("aa", "bb", "cc")
    }
  }

  it should "group by and calculate a sum" in {
    implicit val context = client.context
    implicit val ec = client.ec
    implicit val mat = ActorMaterializer()

    val documents: Seq[BsonDocument] = Seq(
      ("_id" := 1) ~ ("cust_id" := "abc1") ~ ("status" := "A") ~ ("amount" := 50),
      ("_id" := 2) ~ ("cust_id" := "xyz1") ~ ("status" := "A") ~ ("amount" := 100),
      ("_id" := 3) ~ ("cust_id" := "xyz1") ~ ("status" := "D") ~ ("amount" := 25),
      ("_id" := 4) ~ ("cust_id" := "xyz1") ~ ("status" := "D") ~ ("amount" := 125),
      ("_id" := 5) ~ ("cust_id" := "abc1") ~ ("status" := "A") ~ ("amount" := 25)
    )

    val pipeline: List[BsonDocument] = List(
      "$match" := ("status" := "A"),
      "$group" := ("_id" := "$cust_id") ~ ("total" := ("$sum" := "$amount")),
      "$sort" := ("total" := -1)
    )

    val result = for {
      insert <- collection.insert(documents)
      results <- collection.aggregate(pipeline).runFold(List.empty[BsonDocument])(_ ++ _)
    } yield results

    whenReady(result) { results =>
      Logger.debug(s"$results")
      results should have size 2
      results(0).getAs[String]("_id") shouldBe Some("xyz1")
      results(0).getAs[Int]("total") shouldBe Some(100)
      results(1).getAs[String]("_id") shouldBe Some("abc1")
      results(1).getAs[Int]("total") shouldBe Some(75)
    }
  }

  it should "throw WriteException" in {
    implicit val context = client.context
    implicit val ec = client.ec
    val thrown = the[WriteException] thrownBy {
      Await.result(collection.update("name" := "fehmi", "$unknown" := "whatever"), 5.seconds)
    }

    thrown.writeErrors should have size 1
    thrown.writeErrors.head.errmsg shouldBe "Unknown modifier: $unknown"
  }

  it should "handle null value indexing" in {
    implicit val context = client.context
    implicit val ec = client.ec
    implicit val mat = ActorMaterializer()

    val document = ("maybe" := None) ~ ("present" := Some(1)) ~ ("null" := null)

    val result = for {
      insert <- collection.insert(Seq(document))
      list <- collection.find(BsonDocument.empty).runFold(List.empty[BsonDocument])(_ ++ _)
    } yield list

    whenReady(result) { docs =>
      docs.head.get("maybe") shouldBe None
      docs.head.getAs[Int]("maybe") shouldBe None
      docs.head.get("null") shouldBe None
      docs.head.get("doesnotexist") shouldBe None
      docs.head.getAs[Int]("present") shouldBe Some(1)
    }

  }
}
