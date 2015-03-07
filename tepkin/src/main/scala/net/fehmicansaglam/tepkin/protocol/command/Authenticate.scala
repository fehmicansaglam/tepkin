package net.fehmicansaglam.tepkin.protocol.command

import net.fehmicansaglam.bson.util.Converters
import net.fehmicansaglam.bson.{BsonDocument, BsonDsl, Implicits}
import BsonDsl._
import Implicits._
import Converters.md5Hex

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
