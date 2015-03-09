package net.fehmicansaglam.tepkin

import java.io.File

import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers, OptionValues}

import scala.concurrent.duration._

class GridFsSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter {

  override implicit val patienceConfig = PatienceConfig(timeout = 30.seconds, interval = 1.seconds)

  val client = MongoClient("mongodb://localhost")
  val db = client("tepkin")

  import client.ec

  implicit val timeout: Timeout = 30.seconds

  "A GridFs" should "put file" in {
    db.gridFs().put(new File("/Users/fehmicansaglam/Documents/git.pdf"))
    ()
  }

}
