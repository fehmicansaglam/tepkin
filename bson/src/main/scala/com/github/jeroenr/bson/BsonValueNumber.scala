package com.github.jeroenr.bson

trait BsonValueNumber extends BsonValue {
  def toInt: Int

  def toLong: Long

  def toDouble: Double
}
