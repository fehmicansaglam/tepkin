package net.fehmicansaglam.tepkin.examples

import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.MongoClient

import scala.concurrent.duration._

object TailableCursorExample extends App {

  val client = MongoClient("mongodb://localhost")

  import client.{context, ec}

  implicit val timeout: Timeout = 5.seconds
  implicit val mat = ActorFlowMaterializer()

  val db = client("tepkin")
  val messages = db("messages")

  db.createCollection("messages", capped = Some(true), size = Some(100000000))

  messages
    .find(query = BsonDocument.empty, tailable = true)
    .runForeach(println)
}
