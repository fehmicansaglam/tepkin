package net.fehmicansaglam.tepkin.api

import net.fehmicansaglam.bson.{BsonDocument, BsonValue, BsonDsl, Implicits}
import net.fehmicansaglam.bson.element.BsonElement
import BsonDsl._
import Implicits._

import scala.collection.mutable.ArrayBuffer

class BsonDocumentBuilder {

  private var elements = ArrayBuffer.empty[BsonElement]

  def add(name: String, value: BsonValue): BsonDocumentBuilder = {
    elements :+= (name := value)
    this
  }

  def addString(name: String, value: String): BsonDocumentBuilder = {
    elements :+= (name := value)
    this
  }

  def addInt(name: String, value: Int): BsonDocumentBuilder = {
    elements :+= (name := value)
    this
  }

  def addLong(name: String, value: Long): BsonDocumentBuilder = {
    elements :+= (name := value)
    this
  }

  def addDouble(name: String, value: Double): BsonDocumentBuilder = {
    elements :+= (name := value)
    this
  }

  def addBoolean(name: String, value: Boolean): BsonDocumentBuilder = {
    elements :+= (name := value)
    this
  }

  def build(): BsonDocument = {
    BsonDocument(elements)
  }
}
