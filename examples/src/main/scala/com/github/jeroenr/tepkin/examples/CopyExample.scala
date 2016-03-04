package com.github.jeroenr.tepkin.examples

import akka.stream.ActorMaterializer
import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.tepkin.MongoClient

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object CopyExample extends App {
  // Connect to Mongo client
  val client = MongoClient("mongodb://localhost")

  import client.context

  // Obtain reference to database "tepkin" using client
  val db = client("tepkin")

  // Obtain reference to the collection "source" using database
  val source = db("source")

  // Obtain reference to the collection "destination" using database
  val destination = db("destination")

  implicit val mat = ActorMaterializer()

  implicit val timeout: akka.util.Timeout = 60 seconds

  val src = source.find(BsonDocument.empty)
    .mapConcat(identity)
    .groupedWithin(1000, 10 millis) // Mongo batch insert has a maximum of 1000 items
    .map(_.toList)

  private val futureCompletedStream = destination.insertFromSource(src).runForeach(is => println(s"insert result: $is"))

  futureCompletedStream.onComplete {
    case Success(done) => {
      println(s"done $done")
      sys.exit(0)
    }
    case Failure(t) => {
      println(s"failure: $t")
      sys.exit(1)
    }
  }
}
