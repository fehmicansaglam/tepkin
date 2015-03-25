package net.fehmicansaglam.pide

import net.fehmicansaglam.bson.Implicits.ObjectId

trait Entity {
  def id: ObjectId
}
