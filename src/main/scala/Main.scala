import java.util.concurrent.atomic.AtomicInteger

import akka.stream.ActorFlowMaterializer
import akka.util.Timeout
import net.fehmicansaglam.tepkin.MongoClient
import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main {

  def main(args: Array[String]): Unit = {

    val mongoClient = MongoClient("localhost", 27017)
    implicit val mat = ActorFlowMaterializer()(mongoClient.context)

    implicit val timeout: Timeout = 50.seconds

    val index = new AtomicInteger(1)

    1 to 1000 foreach { i =>
      mongoClient("colossus", "abuzer").count().foreach { result =>
        println(s"${index.getAndIncrement}. ${result.n}")
      }
    }

    val result = for {
      count1 <- mongoClient("colossus", "redis").count()
      delete <- mongoClient("colossus", "redis").delete(Seq({
        ("q" := ("name" := "fehmi can") ~ ("surname" := "saglam")) ~
          ("limit" := 0)
      }))
      count2 <- mongoClient("colossus", "redis").count()
    } yield (count1, count2, delete)

    result.map {
      case (count1, count2, delete) =>
        println(delete)
        println(s"before: ${count1.n} after: ${count2.n}")
    }

    val resultCount = for {
      source <- mongoClient("colossus", "abuzer").find(BsonDocument())
      count <- source.runFold(0)((accu, documents) => accu + documents.size)
    } yield count

    resultCount.foreach(count => println(s"-----$count"))

    Thread.sleep(1000)

    mongoClient("colossus", "abuzer").findOne(BsonDocument()).foreach(println)

    mongoClient.shutdown()
  }
}
