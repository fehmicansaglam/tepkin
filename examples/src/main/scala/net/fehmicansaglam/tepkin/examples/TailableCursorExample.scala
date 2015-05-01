package net.fehmicansaglam.tepkin.examples

import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.MongoClient
import net.fehmicansaglam.tepkin.protocol.message.QueryOptions

import scala.concurrent.duration._

object TailableCursorExample extends App {

  val client = MongoClient("mongodb://localhost")

  import client.ec
  import client.context

  implicit val timeout: Timeout = 5.seconds
  implicit val mat = ActorFlowMaterializer()

  val db = client("tepkin")

  db.createCollection("messages", capped = Some(true), size = Some(100000000))

  val messages = db("messages")

  messages.find(query = BsonDocument.empty, flags = QueryOptions.TailableCursor | QueryOptions.AwaitData)
    .flatMap { source =>
    source.runForeach(println)
  }

  client.shutdown()
}
