package net.fehmicansaglam.tepkin.protocol.command

import akka.util.ByteString
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.protocol.message.Message

/**
 * A MongoDB Command.
 *
 * Basically, it is a query that is performed on any db.\$cmd collection.
 */
trait Command extends Message {

  override val responseTo: Int = 0

  override val opCode: Int = 2004

  def databaseName: String

  def command: BsonDocument

  override def encodeBody: ByteString = {
    val flags: Int = 0

    ByteString.newBuilder
      .putInt(flags)
      .putBytes((databaseName + ".$cmd").getBytes("utf-8"))
      .putByte(0)
      .putInt(0) // numberToSkip
      .putInt(1) // numberToReturn
      .append(command.encode)
      .result()
  }
}

/**
 * A command that targets the admin database only (administrative commands).
 */
trait AdminCommand extends Command {
  override def databaseName: String = "admin"
}
