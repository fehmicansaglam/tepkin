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

    1 to 1000 foreach { _ =>
      mongoClient("colossus", "redis").count().foreach { doc =>
        println(s"${index.getAndIncrement}. $doc")
      }
    }
  }
}
