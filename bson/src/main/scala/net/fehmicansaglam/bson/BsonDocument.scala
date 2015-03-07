package net.fehmicansaglam.bson

import akka.util.{ByteString, ByteStringBuilder}
import net.fehmicansaglam.bson.element.BsonElement

case class BsonDocument(elements: BsonElement*) extends BsonValue {

  override def encode(): ByteString = {
    val builder = elements.foldLeft(new ByteStringBuilder) { (builder, element) =>
      builder.append(element.encode())
    }
    builder.putByte(0)

    ByteString.newBuilder
      .putInt(builder.length + 4)
      .append(builder.result())
      .result()
  }

  def ~(element: BsonElement): BsonDocument = BsonDocument(elements :+ element)

  def ~(element: Option[BsonElement]): BsonDocument = element match {
    case Some(element) => BsonDocument(elements :+ element)
    case None => this
  }

  def ++(that: BsonDocument): BsonDocument = BsonDocument(elements ++ that.elements)

  def ++(that: Option[BsonDocument]): BsonDocument = that match {
    case Some(that) => BsonDocument(elements ++ that.elements)
    case None => this
  }

  def get[T <: BsonValue](key: String): Option[T] = {
    elements.find(_.name == key).map(_.value.asInstanceOf[T])
  }

  def getAs[T](key: String): Option[T] = {
    elements.find(_.name == key).map(_.value.asInstanceOf[Identifiable[T]].identifier)
  }

  def getAsList[T](key: String): Option[List[T]] = {
    getAs[BsonDocument](key).map { document =>
      document.elements.map(_.value.asInstanceOf[Identifiable[T]].identifier).toList
    }
  }

  override def toString(): String = s"{ ${elements.mkString(", ")} }"
}

object BsonDocument {

  import net.fehmicansaglam.bson.element.BsonElement

  def apply(elements: TraversableOnce[BsonElement]): BsonDocument = BsonDocument(elements.toSeq: _*)

  def empty: BsonDocument = BsonDocument()
}

