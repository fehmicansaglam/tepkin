package net.fehmicansaglam.tepkin

case class MongoCredentials(username: String, password: Option[String] = None)

case class MongoHost(hostName: String, port: Int = 27017)

case class MongoClientUri(credentials: Option[MongoCredentials] = None,
                          hosts: List[MongoHost],
                          database: Option[String] = None,
                          options: Map[String, String] = Map.empty)
