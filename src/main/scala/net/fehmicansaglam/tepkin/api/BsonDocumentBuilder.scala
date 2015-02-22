package net.fehmicansaglam.tepkin.api

import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.element.BsonElement

import scala.collection.mutable.ArrayBuffer

class BsonDocumentBuilder {

  private var elements = ArrayBuffer.empty[BsonElement]

  def addElement(element: BsonElement): BsonDocumentBuilder = {
    elements :+= element
    this
  }

  def build(): BsonDocument = {
    BsonDocument(elements)
  }
}
