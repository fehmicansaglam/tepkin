package com.github.jeroenr.bson

import com.github.jeroenr.bson.Implicits._
import com.github.jeroenr.bson.element._
import org.joda.time.DateTime

object BsonDsl {

  implicit class BsonField(name: String) {

    def := : PartialFunction[Any, BsonElement] = {
      case null => BsonNull(name)
      case value: Double => BsonDouble(name, value)
      case value: String => BsonString(name, value)
      case value: Boolean => BsonBoolean(name, value)
      case value: DateTime => BsonDateTime(name, value)
      case value: Int => BsonInteger(name, value)
      case value: Long => BsonLong(name, value)
      case value: Option[_] => value.map(name := _).getOrElse($null(name))
      case value: Map[_, _] => BsonObject(name, BsonDocument.from(value.map { case (k, v) => (k.toString, v) }))
      case value: Traversable[_] => BsonArray(name, $array(value.toSeq:_*))
      // For expressions like "$match" := ("status" := "A")
      case value: BsonElement => BsonObject(name, value.toDoc)
      case value: BsonValueDouble => BsonDouble(name, value)
      case value: BsonValueString => BsonString(name, value)
      case value: BsonDocument => BsonObject(name, value)
      case value: BsonValueArray => BsonArray(name, value)
      case value: BsonValueBinary => BsonBinary(name, value)
      case value: BsonValueObjectId => BsonObjectId(name, value)
      case value: BsonValueBoolean => BsonBoolean(name, value)
      case value: BsonValueDateTime => BsonDateTime(name, value)
      case value: BsonValueRegex => BsonRegex(name, value)
      case value: BsonValueInteger => BsonInteger(name, value)
      case value: BsonValueLong => BsonLong(name, value)
    }

  }

  implicit def elementToDocument(element: BsonElement): BsonDocument = BsonDocument(element)

  def $document(elements: BsonElement*): BsonDocument = BsonDocument(elements: _*)

  def $array(values: Any*): BsonValueArray = BsonValueArray(BsonDocument(
    values.zipWithIndex.map {
      case (value, index) => s"$index" := value
    }: _*))

  def $array(values: Traversable[_]): BsonValueArray = $array(values.toSeq: _*)

  def $or(documents: BsonDocument*): BsonElement = "$or" := $array(documents: _*)

  def $set(document: BsonDocument): BsonElement = "$set" := document

  def $unset(field: String, fields: String*): BsonElement = "$unset" := BsonDocument(fields.+:(field).map(_ := ""))

  def $query(document: BsonDocument): BsonElement = "$query" := document

  def $orderBy(document: BsonDocument): BsonElement = "$orderBy" := document

  def $null(name: String): BsonNull = BsonNull(name)
}
