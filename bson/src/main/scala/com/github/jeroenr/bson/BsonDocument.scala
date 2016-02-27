package com.github.jeroenr.bson

import akka.util.{ByteString, ByteStringBuilder}
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.element.{BsonNullValue, BsonNull, BsonElement, BsonObject}

import language.postfixOps

case class BsonDocument(private val elems: BsonElement*) extends BsonValue {

  val elements = elems.filterNot(_.value == BsonNullValue)

  protected lazy val flat: Seq[(String, BsonValue)] = elements.flatMap {
    case element@BsonObject(name, value) =>
      value.identifier.flat.map { case (k, v) => s"$name.$k" -> v } :+ element.toTuple
    case element => Seq(element.toTuple)
  }

  protected lazy val internal: Map[String, BsonValue] = flat.toMap

  override def encode: ByteString = {
    val builder = elements.foldLeft(new ByteStringBuilder) { (builder, element) =>
      builder.append(element.encode)
    }
    builder.putByte(0)

    ByteString.newBuilder
      .putInt(builder.length + 4)
      .append(builder.result())
      .result()
  }

  def ~(element: BsonElement): BsonDocument = BsonDocument(elements :+ element)

  def ~(element: Option[BsonElement]): BsonDocument = element match {
    case Some(_element) => BsonDocument(elements :+ _element)
    case None => this
  }

  def ++(that: BsonDocument): BsonDocument = BsonDocument(elements ++ that.elements)

  def ++(that: Option[BsonDocument]): BsonDocument = that match {
    case Some(_that) => BsonDocument(elements ++ _that.elements)
    case None => this
  }

  /**
   * Supports nested documents with . operator i.e. `foo.bar.baz`
   */
  def get[T <: BsonValue](key: String): Option[T] = {
    internal.get(key).map(_.asInstanceOf[T])
  }

  /**
   * Supports nested documents with . operator i.e. `foo.bar.baz`
   */
  def getAs[T](key: String): Option[T] = {
    internal.get(key).map(_.asInstanceOf[Identifiable[T]].identifier)
  }

  /**
   * Supports nested documents with . operator i.e. `foo.bar.baz`
   */
  def getAsList[T](key: String): Option[List[T]] = {
    getAs[BsonDocument](key).map { document =>
      document.elements.map(_.value.asInstanceOf[Identifiable[T]].identifier).toList
    }
  }

  override def toString: String = s"{ ${elements.mkString(", ")} }"

  override def pretty(level: Int): String = {
    val prefix = "\t" * level
    val init = elements.init.foldLeft("")(_ + _.pretty(level + 1) + s",\n")
    val last = elements.last.pretty(level + 1)
    s"{\n$init$last\n$prefix}"
  }

  override def toJson(extended: Boolean): String = s"{ ${elements.map(_.toJson(extended)).mkString(", ")} }"
}

object BsonDocument {

  def apply(elements: TraversableOnce[BsonElement]): BsonDocument = apply(elements.toSeq: _*)

  def from(elements: (String, Any)*): BsonDocument = apply(elements.map { case (k, v) => k := v }: _*)

  def from(elements: TraversableOnce[(String, Any)]): BsonDocument = from(elements.toSeq: _*)

  def from(map: Map[String, _]): BsonDocument = from(map.toSeq: _*)

  val empty: BsonDocument = BsonDocument()
}

case class Bulk(documents: List[BsonDocument]) {
  def size: Int = documents.size

  def isEmpty: Boolean = documents.isEmpty

  def head: BsonDocument = documents.head

  def headOption: Option[BsonDocument] = documents.headOption
}
