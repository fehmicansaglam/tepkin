package net.fehmicansaglam.bson

import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.element._
import org.joda.time.DateTime

object BsonDsl {

  implicit class BsonField(name: String) {

    def := : PartialFunction[Any, BsonElement] = {
      case value: Double => BsonDouble(name, value)
      case value: String => BsonString(name, value)
      case value: Boolean => BsonBoolean(name, value)
      case value: DateTime => BsonDateTime(name, value)
      case value: Int => BsonInteger(name, value)
      case value: Long => BsonLong(name, value)
      case value: List[_] => BsonArray(name, $array(value))
      case value: Map[_, _] => BsonObject(name, $map(value))
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

    def :=[A](value: Option[A])(implicit ev: A => BsonValue): Option[BsonElement] = value map (name := _)


  }

  implicit def elementToDocument(element: BsonElement): BsonDocument = BsonDocument(element)

  def $document(elements: BsonElement*): BsonDocument = BsonDocument(elements: _*)

  def $map(map: Map[_, _]): BsonDocument =
    BsonDocument(map.map {
      case (k:String, null) => BsonNull(k)
      case (k:String, v:Int) => BsonInteger(k, v)
      case (k:String, v:Double) => BsonDouble(k, v)
      case (k:String, v:Boolean) => BsonBoolean(k, v)
      case (k:String, v:Long) => BsonLong(k, v)
      case (k:String, v:String) => BsonString(k, v)
      case (k:String, v:List[_]) => BsonArray(k, $array(v))
      case (k:String, v:Map[_, _]) => BsonObject(k, $map(v))
    })

  def $array(values: Any*): BsonValueArray = BsonValueArray(BsonDocument(values.zipWithIndex.map {
    case (value:List[_], index) => s"$index" := $array(value)
    case (value:Map[_, _], index) => s"$index" := $map(value)
    case (value, index) => s"$index" := value
  }: _*))

  def $array(values: List[_]): BsonValueArray = $array(values: _*)

  def $or(documents: BsonDocument*): BsonElement = "$or" := $array(documents: _*)

  def $set(document: BsonDocument): BsonElement = "$set" := document

  def $unset(field: String, fields: String*): BsonElement = "$unset" := BsonDocument(fields.+:(field).map(_ := ""))

  def $query(document: BsonDocument): BsonElement = "$query" := document

  def $orderBy(document: BsonDocument): BsonElement = "$orderBy" := document
}
