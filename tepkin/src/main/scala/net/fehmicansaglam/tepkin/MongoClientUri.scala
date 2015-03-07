package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import net.fehmicansaglam.tepkin.MongoClientUriParser._

case class MongoCredentials(username: String, password: Option[String] = None)

case class MongoClientUri(credentials: Option[MongoCredentials] = None,
                          hosts: Set[InetSocketAddress],
                          database: Option[String] = None,
                          options: Map[String, String] = Map.empty)

object MongoClientUri {
  def apply(input: String): MongoClientUri = MongoClientUriParser.parseAll(uri, input) match {
    case Success(mongoUri, _) => mongoUri
    case failure: NoSuccess => throw new IllegalArgumentException(failure.msg)
  }
}
