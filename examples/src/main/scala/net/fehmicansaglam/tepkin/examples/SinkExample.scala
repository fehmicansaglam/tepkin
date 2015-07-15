package net.fehmicansaglam.tepkin.examples

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.{BsonDocument, Bulk}
import net.fehmicansaglam.tepkin.MongoClient

import scala.collection.immutable.Iterable

object SinkExample extends App {
  // Connect to Mongo client
  val client = MongoClient("mongodb://localhost")

  import client.context

  // Obtain reference to database "tepkin" using client
  val db = client("tepkin")

  // Obtain reference to the collection "collection1" using database
  val collection1 = db("collection1")

  // Obtain reference to the collection "collection2" using database
  val collection2 = db("collection2")

  implicit val mat = ActorMaterializer()

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
