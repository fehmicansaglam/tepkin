package com.github.jeroenr.tepkin

import java.net.InetSocketAddress

import com.github.jeroenr.tepkin.MongoClientUriParser._

case class MongoCredentials(username: String, password: Option[String] = None)

/**
 *
 * @param credentials Optional. If specified, the client will attempt to log in to the specific database using these
 *                    credentials after connecting to the mongod instance.
 * @param hosts You can specify as many hosts as necessary. You would specify multiple hosts, for example, for
 *              connections to replica sets.
 * @param database Optional. The name of the database to authenticate if the connection string includes authentication
 *                 credentials in the form of username:password@. If /database is not specified and the connection
 *                 string includes credentials, the driver will authenticate to the admin database.
 * @param options Connection specific options.
 */
case class MongoClientUri(credentials: Option[MongoCredentials] = None,
                          hosts: Set[InetSocketAddress],
                          database: Option[String] = None,
                          options: Map[String, String] = Map.empty) {
  def option(key: String): Option[String] = options.get(key)
}

object MongoClientUri {
  def apply(input: String): MongoClientUri = MongoClientUriParser.parseAll(uri, input) match {
    case Success(mongoUri, _) => mongoUri
    case failure: NoSuccess => throw new IllegalArgumentException(failure.msg)
  }
}
