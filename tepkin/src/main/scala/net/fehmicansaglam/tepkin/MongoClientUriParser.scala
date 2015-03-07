package net.fehmicansaglam.tepkin

import java.net.InetSocketAddress

import scala.util.parsing.combinator.RegexParsers

object MongoClientUriParser extends RegexParsers {
  def credential: Parser[String] = """[^:@?]+""".r ^^ {
    _.toString
  }

  def hostName: Parser[String] = """[^:,?/]+""".r ^^ {
    _.toString
  }

  def port: Parser[Int] = """[0-9]+""".r ^^ {
    _.toInt
  }

  def database: Parser[String] = """[^?]+""".r ^^ {
    _.toString
  }

  def option: Parser[(String, String)] = """[^=]+""".r ~ "=" ~ """[^&]+""".r ^^ {
    case key ~ _ ~ value => key -> value
  }

  def options: Parser[Map[String, String]] = option ~ rep("&" ~ option) ^^ {
    case head ~ tail => (head +: tail.map(_._2)).toMap
  }

  def credentials: Parser[MongoCredentials] = credential ~ opt(":" ~ credential) ^^ {
    case username ~ None =>
      MongoCredentials(username = username)
    case username ~ Some(":" ~ password) =>
      MongoCredentials(username = username, password = Some(password))
  }

  def host: Parser[InetSocketAddress] = hostName ~ opt(":" ~ port) ^^ {
    case hostName ~ None => new InetSocketAddress(hostName, 27017)
    case hostName ~ Some(":" ~ port) => new InetSocketAddress(hostName, port)
  }

  def uri: Parser[MongoClientUri] = {
    "mongodb://" ~ opt(credentials ~ "@") ~ host ~ rep("," ~ host) ~ opt("/" ~ opt(database) ~ opt("?" ~ options)) ^^ {
      case _ ~ credentials ~ host ~ hosts ~ None =>
        MongoClientUri(
          credentials = credentials.map(_._1),
          hosts = hosts.map(_._2).toSet + host
        )

      case _ ~ credentials ~ host ~ hosts ~ Some(_ ~ database ~ options) =>
        MongoClientUri(
          credentials = credentials.map(_._1),
          hosts = hosts.map(_._2).toSet + host,
          database = database,
          options = options.map(_._2).getOrElse(Map.empty)
        )
    }
  }
}
