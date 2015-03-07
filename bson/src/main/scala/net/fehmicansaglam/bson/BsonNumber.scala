package net.fehmicansaglam.bson

trait BsonNumber extends BsonValue {
  def toInt: Int

  def toLong: Long

  def toDouble: Double
}
