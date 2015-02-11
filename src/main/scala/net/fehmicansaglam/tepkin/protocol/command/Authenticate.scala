package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.bson.BsonDsl._
import net.fehmicansaglam.tepkin.bson.Implicits._
import net.fehmicansaglam.tepkin.util.Converters.md5Hex

case class Authenticate(databaseName: String,
                        username: String,
                        password: String,
                        nonce: String) extends Command {
  override val command: BsonDocument = {
    ("authenticate" := 1) ~
      ("user" := username) ~
      ("nonce" := nonce) ~
      ("key" := md5Hex(nonce + username + md5Hex(s"${username}:mongo:${password}")))
  }
}
