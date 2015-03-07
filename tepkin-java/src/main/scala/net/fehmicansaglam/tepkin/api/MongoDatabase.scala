package net.fehmicansaglam.tepkin.api

import net.fehmicansaglam.tepkin

class MongoDatabase(proxy: tepkin.MongoDatabase) {

  def collection(collectionName: String): MongoCollection = {
    new MongoCollection(proxy.apply(collectionName))
  }
}

