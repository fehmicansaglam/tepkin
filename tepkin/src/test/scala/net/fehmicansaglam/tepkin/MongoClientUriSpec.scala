package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import org.scalatest.{FlatSpec, Matchers}

class MongoClientUriSpec extends FlatSpec with Matchers {

  "A MongoClientUri" should "parse database server running locally" in {
    val expected = MongoClientUri(hosts = Set(new InetSocketAddress("localhost", 27017)))
    val actual = MongoClientUri("mongodb://localhost")

    actual shouldBe expected
  }

  it should "parse admin database" in {
    val expected = MongoClientUri(
      credentials = Some(MongoCredentials(username = "sysop", password = Some("moon"))),
      hosts = Set(new InetSocketAddress("localhost", 27017))
    )
    val actual = MongoClientUri("mongodb://sysop:moon@localhost")

    actual shouldBe expected
  }

  it should "parse replica set with members on different machines" in {
    val expected = MongoClientUri(
      hosts = Set(
        new InetSocketAddress("db1.example.net", 27017),
        new InetSocketAddress("db2.example.com", 27017)
      ))
    val actual = MongoClientUri("mongodb://db1.example.net,db2.example.com")

    actual shouldBe expected
  }

  it should "parse replica set with members on localhost" in {
    val expected = MongoClientUri(
      hosts = Set(
        new InetSocketAddress("localhost", 27017),
        new InetSocketAddress("localhost", 27018),
        new InetSocketAddress("localhost", 27019)
      ))
    val actual = MongoClientUri("mongodb://localhost,localhost:27018,localhost:27019")

    actual shouldBe expected
  }

  it should "parse replica set with read distribution" in {
    val expected = MongoClientUri(
      hosts = Set(
        new InetSocketAddress("example1.com", 27017),
        new InetSocketAddress("example2.com", 27017),
        new InetSocketAddress("example3.com", 27017)
      ),
      options = Map("readPreference" -> "secondary")
    )
    val actual = MongoClientUri("mongodb://example1.com,example2.com,example3.com/?readPreference=secondary")

    actual shouldBe expected
  }

  it should "parse replica set with a high level of write concern" in {
    val expected = MongoClientUri(
      hosts = Set(
        new InetSocketAddress("example1.com", 27017),
        new InetSocketAddress("example2.com", 27017),
        new InetSocketAddress("example3.com", 27017)
      ),
      options = Map("w" -> "2", "wtimeoutMS" -> "2000")
    )
    val actual = MongoClientUri("mongodb://example1.com,example2.com,example3.com/?w=2&wtimeoutMS=2000")

    actual shouldBe expected
  }

  it should "fail on invalid connection string" in {
    intercept[IllegalArgumentException] {
      MongoClientUri("mongodb://example1.com,example2.com,example3.com?w=2&wtimeoutMS=2000")
    }
    ()
  }
}
