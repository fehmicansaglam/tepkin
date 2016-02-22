package com.github.jeroenr.bson

import BsonDsl._
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
  }
}
