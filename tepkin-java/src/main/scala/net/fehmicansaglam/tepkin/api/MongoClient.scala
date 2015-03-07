package net.fehmicansaglam.tepkin.api

import net.fehmicansaglam.tepkin

import scala.concurrent.ExecutionContext

class MongoClient(proxy: tepkin.MongoClient) {

  def ec: ExecutionContext = proxy.ec

  def db(databaseName: String): MongoDatabase = {
    new MongoDatabase(proxy.apply(databaseName))
  }

  def shutdown(): Unit = {
    proxy.shutdown()
  }
}

object MongoClient {

  /**
   * To be used from Java API.
   */
  def create(uri: String): MongoClient = {
    new MongoClient(tepkin.MongoClient(uri))
  }

}
