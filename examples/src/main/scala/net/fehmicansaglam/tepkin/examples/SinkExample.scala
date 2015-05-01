package net.fehmicansaglam.tepkin.examples

import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.tepkin.MongoClient
import net.fehmicansaglam.tepkin.TepkinMessage.Bulk

import scala.collection.immutable.Iterable
import scala.concurrent.duration._

object SinkExample extends App {
  val begin = System.currentTimeMillis()

  // Connect to Mongo client
  val client = MongoClient("mongodb://localhost")

  import client.context

  // Obtain reference to database "tepkin" using client
  val db = client("tepkin")

  // Obtain reference to the collection "collection1" using database
  val collection1 = db("collection1")

  // Obtain reference to the collection "collection2" using database
  val collection2 = db("collection2")

  implicit val timeout: Timeout = 30.seconds
  implicit val mat = ActorFlowMaterializer()

  // Batch document source
  def documents(n: Int): Source[List[BsonDocument], Unit] = Source {
    Iterable.tabulate(n) { _ =>
      (1 to 1000).map(i => $document("name" := s"fehmi$i")).toList
    }
  }

  val ref1 = documents(1000).map(Bulk).runWith(collection1.sink())
  val ref2 = documents(2000).map(Bulk).runWith(collection2.sink())

  client.shutdown(ref1, ref2)
}
