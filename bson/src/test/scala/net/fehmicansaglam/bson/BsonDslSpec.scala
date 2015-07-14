package net.fehmicansaglam.bson

import net.fehmicansaglam.bson.BsonDsl._
import org.scalatest.{Matchers, WordSpec}

class BsonDslSpec extends WordSpec with Matchers {

  "BsonDsl" must {

    "create $unset document" in {
      val expected = "$unset" := {
        ("field1" := "") ~ ("field2" := "")
      }
      val actual = $unset("field1", "field2")
      actual should be(expected)
    }

    "handle (deeply) nested structures" in {
      val map = Map(
        "level0" -> 1,
        "level1" -> Map(
          "level2" -> "2"
        )
      )
      val doc = BsonDocument.from(map.toIterable)
      doc.toJson() should be("""{ "level0": 1, "level1": { "level2": "2" } }""")

      val map2 = Map(
        "level1" -> List(Map(
          "level2" -> "2"
        )),
        "listoflists" -> List(List(1,2), List(3,4))
      )
      val doc2 = BsonDocument.from(map2.toIterable)
      doc2.toJson() should be("""{ "level1": [ { "level2": "2" } ], "listoflists": [ [ 1, 2 ], [ 3, 4 ] ] }""")

    }
  }
}
