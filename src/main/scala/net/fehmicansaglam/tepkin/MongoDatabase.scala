package net.fehmicansaglam.tepkin

import akka.actor.ActorRef

class MongoDatabase(pool: ActorRef, databaseName: String) {

  def apply(collectionName: String): MongoCollection = {
    new MongoCollection(databaseName, collectionName, pool)
  }

  def collection(collectionName: String): api.MongoCollection = {
    new api.MongoCollection(apply(collectionName))
  }
}

