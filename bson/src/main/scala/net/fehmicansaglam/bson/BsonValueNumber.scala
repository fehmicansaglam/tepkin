package net.fehmicansaglam.bson

trait BsonValueNumber extends BsonValue {
  def toInt: Int

  def toLong: Long

  def toDouble: Double
}
