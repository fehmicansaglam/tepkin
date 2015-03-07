package net.fehmicansaglam.tepkin

import akka.actor.ActorRef

class MongoDatabase(pool: ActorRef, databaseName: String) {

  def apply(collectionName: String): MongoCollection = {
    new MongoCollection(databaseName, collectionName, pool)
  }

  def collection = apply _
}

