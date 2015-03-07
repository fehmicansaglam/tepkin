package net.fehmicansaglam.tepkin.api

import java.net.InetSocketAddress

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

  import scala.collection.JavaConverters._

  def create(seed: InetSocketAddress): MongoClient = {
    create(Set(seed).asJava)
  }

  /**
   * To be used from Java API.
   * @param seeds known nodes in the replicaset.
   */
  def create(seeds: java.util.Set[InetSocketAddress]): MongoClient = {
    new MongoClient(tepkin.MongoClient(seeds.asScala.toSet))
  }

}
