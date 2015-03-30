package net.fehmicansaglam.bson

import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.element.BsonObjectId
import net.fehmicansaglam.bson.reader.BsonDocumentReader
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}

class BsonSpec extends WordSpec with Matchers {

  "Bson" must {

    "encode and decode BsonDocument" in {
      val expected = $document(
        "_id" := BsonObjectId.generate,
        "name" := "jack",
        "age" := 18,
        "months" := $array(1, 2, 3),
        "details" := $document(
          "salary" := 455.5,
          "inventory" := $array("a", 3.5, 1L, true),
          "birthday" := new DateTime(1987, 3, 5, 0, 0)
        )
      )

      val encoded = expected.encode
      val actual = BsonDocumentReader(encoded).read
      actual should be(Some(expected))
    }
  }
}
