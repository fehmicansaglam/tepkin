package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

case class MongoCredentials(username: String, password: Option[String] = None)

case class MongoClientUri(credentials: Option[MongoCredentials] = None,
                          hosts: List[InetSocketAddress],
                          database: Option[String] = None,
                          options: Map[String, String] = Map.empty)
