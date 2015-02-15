import java.util.concurrent.atomic.AtomicInteger

import akka.util.Timeout
import net.fehmicansaglam.tepkin.MongoClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main {

  def main(args: Array[String]): Unit = {
    val mongoClient = MongoClient("localhost", 27017)

    implicit val timeout: Timeout = 50.seconds

    val index = new AtomicInteger(1)

    val start = System.currentTimeMillis()
    1 to 10000 foreach { i =>
      mongoClient("colossus", "redis").count().foreach { doc =>
        println(s"${index.getAndIncrement}. $doc")
        println(System.currentTimeMillis() - start)
      }
    }

    mongoClient.shutdown()
  }
}
