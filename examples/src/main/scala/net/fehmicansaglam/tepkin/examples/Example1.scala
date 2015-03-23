package net.fehmicansaglam.tepkin.examples

import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.tepkin.MongoClient

import scala.collection.immutable.Iterable
import scala.concurrent.Await
import scala.concurrent.duration._

object Example1 extends App {
  val begin = System.currentTimeMillis()

  // Connect to Mongo client
  val client = MongoClient("mongodb://localhost")

  // Use client's execution context for async operations

  import client.ec

  // Obtain reference to database "tepkin" using client
  val db = client("tepkin")

  // Obtain reference to the collection "collection1" using database
  val collection1 = db("collection1")

  // Obtain reference to the collection "collection2" using database
  val collection2 = db("collection2")

  implicit val timeout: Timeout = 30.seconds
  implicit val mat = ActorFlowMaterializer()(client.context)

  // Batch document source
  def documents(n: Int): Source[List[BsonDocument], Unit] = Source {
    Iterable.tabulate(n) { _ =>
      (1 to 1000).map(i => $document("name" := s"fehmi$i")).toList
    }
  }

  // Insert 3M documents and then read them all.
  val futureResult = for {
    delete1 <- collection1.drop()
    delete2 <- collection2.drop()
    insert1 <- collection1.insertFromSource(documents(1000)).runForeach(_ => ())
    insert2 <- collection2.insertFromSource(documents(2000)).runForeach(_ => ())
    source1 <- collection1.find(BsonDocument.empty, batchMultiplier = 10000)
    source2 <- collection2.find(BsonDocument.empty, batchMultiplier = 10000)
    fold1 = source1.runFold(0) { (total, documents) =>
      total + documents.size
    }
    fold2 = source2.runFold(0) { (total, documents) =>
      total + documents.size
    }
    result1 <- fold1
    result2 <- fold2
  } yield (result1, result2)

  val result = Await.result(futureResult, 90.seconds)

  println(s"collection1: ${result._1}")
  println(s"collection2: ${result._2}")
  println(s"Elapsed: ${System.currentTimeMillis() - begin}ms")

  // Drop created collections
  Await.ready(collection1.drop(), 10.seconds)
  Await.ready(collection2.drop(), 10.seconds)

  client.shutdown()
}
