package net.fehmicansaglam.bson

import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.element._

object BsonDsl {

  implicit class BsonField(name: String) {

    def :=(value: BsonValue): BsonElement = value match {
      case value: BsonValueDouble => BsonDouble(name, value)
      case value: BsonValueString => BsonString(name, value)
      case value: BsonDocument => BsonObject(name, value)
      case value: BsonValueArray => BsonArray(name, value)
      case value: BsonValueBinary => BsonBinary(name, value)
      case value: BsonValueObjectId => BsonObjectId(name, value)
      case value: BsonValueBoolean => BsonBoolean(name, value)
      case value: BsonValueDateTime => BsonDateTime(name, value)
      case value: BsonValueInteger => BsonInteger(name, value)
      case value: BsonValueLong => BsonLong(name, value)
    }

    def :=[A](value: Option[A])(implicit ev: A => BsonValue): Option[BsonElement] = value map (name := _)
  }

  implicit def elementToDocument(element: BsonElement): BsonDocument = BsonDocument(element)

  def $document(elements: BsonElement*): BsonDocument = BsonDocument(elements: _*)

  def $array(values: BsonValue*): BsonValueArray = BsonValueArray(BsonDocument(
    values.zipWithIndex.map {
      case (value, index) => s"$index" := value
    }: _*))

  def $or(documents: BsonDocument*): BsonElement = "$or" := $array(documents: _*)

  def $set(document: BsonDocument): BsonElement = "$set" := document
}
