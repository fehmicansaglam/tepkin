package net.fehmicansaglam.tepkin.java

import net.fehmicansaglam.tepkin

class MongoDatabase(proxy: tepkin.MongoDatabase) {

  def collection(collectionName: String): MongoCollection = {
    new MongoCollection(proxy.apply(collectionName))
  }
}

