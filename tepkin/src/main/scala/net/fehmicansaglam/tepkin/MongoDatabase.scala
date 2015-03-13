package net.fehmicansaglam.tepkin

import akka.actor.ActorRef

class MongoDatabase(pool: ActorRef, databaseName: String) {

  def apply(collectionName: String): MongoCollection = {
    require(collectionName != null && collectionName.getBytes("UTF-8").size < 123,
      "Collection name must be shorter than 123 bytes")
    new MongoCollection(databaseName, collectionName, pool)
  }

  def gridFs(prefix: String = "fs"): GridFs = {
    new GridFs(this, prefix)
  }

  def collection = apply _
}

