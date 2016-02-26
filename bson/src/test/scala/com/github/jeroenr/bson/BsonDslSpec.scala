package com.github.jeroenr.bson

import BsonDsl._
import com.github.jeroenr.bson.element.{BsonNullValue, BsonObject}
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

    "handle collections" in {
      val doc = BsonDocument("items" := (1 to 10))
      doc.getAsList[Int]("items") shouldBe Some(1 to 10)
    }

    "handle Some" in {
      val doc = BsonDocument("maybe" := Some(1))
      doc.getAs[Int]("maybe") shouldBe Some(1)
    }

    "handle None" in {
      val doc = BsonDocument("maybe" := None, "present" := Some(1))
      doc.get("maybe") shouldBe None
      doc.getAs[Int]("maybe") shouldBe None
      doc.getAs[Int]("present") shouldBe Some(1)
    }
  }
}
