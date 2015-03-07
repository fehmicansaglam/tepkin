package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import org.scalatest.{FlatSpec, Matchers}

class MongoClientUriParserSpec extends FlatSpec with Matchers {

  "A MongoUriParser" should "parse database server running locally" in {
    val expected = MongoClientUri(hosts = List(new InetSocketAddress("localhost", 27017)))
    val actual = MongoClientUriParser("mongodb://localhost")

    actual shouldBe expected
  }

  it should "parse admin database" in {
    val expected = MongoClientUri(
      credentials = Some(MongoCredentials(username = "sysop", password = Some("moon"))),
      hosts = List(new InetSocketAddress("localhost", 27017))
    )
    val actual = MongoClientUriParser("mongodb://sysop:moon@localhost")

    actual shouldBe expected
  }

  it should "parse replica set with members on different machines" in {
    val expected = MongoClientUri(
      hosts = List(
        new InetSocketAddress("db1.example.net", 27017),
        new InetSocketAddress("db2.example.com", 27017)
      ))
    val actual = MongoClientUriParser("mongodb://db1.example.net,db2.example.com")

    actual shouldBe expected
  }

  it should "parse replica set with members on localhost" in {
    val expected = MongoClientUri(
      hosts = List(
        new InetSocketAddress("localhost", 27017),
        new InetSocketAddress("localhost", 27018),
        new InetSocketAddress("localhost", 27019)
      ))
    val actual = MongoClientUriParser("mongodb://localhost,localhost:27018,localhost:27019")

    actual shouldBe expected
  }

  it should "parse replica set with read distribution" in {
    val expected = MongoClientUri(
      hosts = List(
        new InetSocketAddress("example1.com", 27017),
        new InetSocketAddress("example2.com", 27017),
        new InetSocketAddress("example3.com", 27017)
      ),
      options = Map("readPreference" -> "secondary")
    )
    val actual = MongoClientUriParser("mongodb://example1.com,example2.com,example3.com/?readPreference=secondary")

    actual shouldBe expected
  }

  it should "parse replica set with a high level of write concern" in {
    val expected = MongoClientUri(
      hosts = List(
        new InetSocketAddress("example1.com", 27017),
        new InetSocketAddress("example2.com", 27017),
        new InetSocketAddress("example3.com", 27017)
      ),
      options = Map("w" -> "2", "wtimeoutMS" -> "2000")
    )
    val actual = MongoClientUriParser("mongodb://example1.com,example2.com,example3.com/?w=2&wtimeoutMS=2000")

    actual shouldBe expected
  }

  it should "fail on invalid connection string" in {
    intercept[IllegalArgumentException] {
      MongoClientUriParser("mongodb://example1.com,example2.com,example3.com?w=2&wtimeoutMS=2000")
    }
    ()
  }
}
