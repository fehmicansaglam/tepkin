package net.fehmicansaglam.tepkin

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

  def host: Parser[MongoHost] = hostName ~ opt(":" ~ port) ^^ {
    case hostName ~ None => MongoHost(hostName = hostName)
    case hostName ~ Some(":" ~ port) => MongoHost(hostName = hostName, port = port)
  }

  def uri: Parser[MongoClientUri] = {
    "mongodb://" ~ opt(credentials ~ "@") ~ host ~ rep("," ~ host) ~ opt("/" ~ opt(database) ~ opt("?" ~ options)) ^^ {
      case _ ~ credentials ~ host ~ hosts ~ None =>
        MongoClientUri(
          credentials = credentials.map(_._1),
          hosts = host +: hosts.map(_._2)
        )

      case _ ~ credentials ~ host ~ hosts ~ Some(_ ~ database ~ options) =>
        MongoClientUri(
          credentials = credentials.map(_._1),
          hosts = host +: hosts.map(_._2),
          database = database,
          options = options.map(_._2).getOrElse(Map.empty)
        )
    }
  }

  def apply(input: String): MongoClientUri = parseAll(uri, input) match {
    case Success(mongoUri, _) => mongoUri
    case failure: NoSuccess => throw new IllegalArgumentException(failure.msg)
  }
}
