package com.github.jeroenr.tepkin.examples

import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.tepkin.MongoClient

import scala.concurrent.duration._

object TailableCursorExample extends App {

  val client = MongoClient("mongodb://localhost")

  import client.{context, ec}

  implicit val timeout: Timeout = 5.seconds
  implicit val mat = ActorMaterializer()

  val db = client("tepkin")
  val messages = db("messages")

  db.createCollection("messages", capped = Some(true), size = Some(100000000))

  messages
    .find(query = BsonDocument.empty, tailable = true)
    .runForeach(println)
}
