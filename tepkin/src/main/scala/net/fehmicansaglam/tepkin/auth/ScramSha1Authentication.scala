package net.fehmicansaglam.tepkin.auth

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp.{Received, Write}
import akka.util.ByteString
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.element.BinarySubtype
import net.fehmicansaglam.bson.util.Converters
import net.fehmicansaglam.bson.{BsonDocument, BsonValueNumber}
import net.fehmicansaglam.tepkin.MongoCredentials
import net.fehmicansaglam.tepkin.protocol.command.Command
import net.fehmicansaglam.tepkin.protocol.message.Reply
import net.fehmicansaglam.tepkin.util.{Crypto, Randomizer}

trait ScramSha1Authentication extends Authentication with Crypto with Randomizer {
  this: Actor with ActorLogging =>

  private[this] case class Step0(rPrefix: String,
                                 clientFirstMessageBare: String,
                                 clientFirstMessage: Array[Byte])

  private[this] case class Step1(serverSignature: Array[Byte],
                                 clientFinalMessage: Array[Byte])

  private[this] case class SaslStart(databaseName: String, message: ByteString) extends Command {
    override def command: BsonDocument = {
      ("saslStart" := 1) ~
        ("mechanism" := "SCRAM-SHA-1") ~
        ("payload" := Binary(message, BinarySubtype.Generic))
    }
  }

  private[this] case class SaslContinue(databaseName: String, conversationId: Int, message: ByteString) extends Command {
    override def command: BsonDocument = {
      ("saslContinue" := 1) ~
        ("conversationId" := conversationId) ~
        ("payload" := Binary(message, BinarySubtype.Generic))
    }
  }

  private val GS2_HEADER = "n,,"
  private val RANDOM_LENGTH = 24

  private def prepareUsername(username: String): String = {
    username.replace("=", "=3D").replace(",", "=2D")
  }

  private def parseServerResponse(response: String): Map[String, String] = {
    response.split(",").map(_.split("=", 2)).map(array => (array(0), array(1))).toMap
  }

  private def computeStep0(username: String): Step0 = {
    val preparedUsername = "n=" + prepareUsername(username)
    val rPrefix = randomString(RANDOM_LENGTH)
    val nonce = s"r=$rPrefix"
    val clientFirstMessageBare = s"$preparedUsername,$nonce"
    val clientFirstMessage = GS2_HEADER + clientFirstMessageBare
    Step0(
      rPrefix = rPrefix,
      clientFirstMessageBare = clientFirstMessageBare,
      clientFirstMessage = decodeUtf8(clientFirstMessage)
    )
  }

  private def computeStep1(challenge: Array[Byte], username: String, password: String, step0: Step0): Step1 = {
    val serverFirstMessage = encodeUtf8(challenge)
    val response = parseServerResponse(serverFirstMessage)
    val r = response("r")

    if (!r.startsWith(step0.rPrefix)) {
      throw new RuntimeException("Server sent an invalid nonce.")
    }

    val s = response("s")
    val iterations = response("i")

    val channelBinding = "c=" + encodeBase64(decodeUtf8(GS2_HEADER))
    val nonce = s"r=$r"
    val clientFinalMessageWithoutProof = s"$channelBinding,$nonce"

    val saltedPassword = keyDerive(Converters.md5Hex(s"$username:mongo:$password"), decodeBase64(s), iterations.toInt)
    val clientKey = hmac(saltedPassword, "Client Key")
    val storedKey = sha1(clientKey)
    val authMessage = step0.clientFirstMessageBare + "," + serverFirstMessage + "," + clientFinalMessageWithoutProof
    val clientSignature = hmac(storedKey, authMessage)
    val clientProof = xor(clientKey, clientSignature)
    val serverKey = hmac(saltedPassword, "Server Key")
    val serverSignature = hmac(serverKey, authMessage)

    val proof = "p=" + encodeBase64(clientProof)
    val clientFinalMessage = clientFinalMessageWithoutProof + "," + proof

    Step1(
      serverSignature = serverSignature,
      clientFinalMessage = decodeUtf8(clientFinalMessage)
    )
  }

  private def computeStep2(challenge: ByteString, step1: Step1): ByteString = {
    val response = parseServerResponse(encodeUtf8(challenge.toArray))

    if (response("v") != encodeBase64(step1.serverSignature)) {
      throw new RuntimeException("Server signature was invalid.")
    }

    challenge
  }

  override def authenticate(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials]): Unit = {
    val step0 = computeStep0(credentials.get.username)
    val command = SaslStart(databaseName, ByteString(step0.clientFirstMessage))
    context.become(step1(connection, databaseName, credentials, step0))
    connection ! Write(command.encode)
  }

  private def step1(connection: ActorRef, databaseName: String, credentials: Option[MongoCredentials], step0: Step0): Receive = {
    case Received(data) =>
      for {
        reply <- Reply(data.asByteBuffer)
        result <- reply.documents.headOption
        conversationId <- result.getAs[Int]("conversationId")
        payload <- result.get[Binary]("payload")
      } {
        val step1 = computeStep1(payload.value.toArray, credentials.get.username, credentials.get.password.get, step0)
        val command = SaslContinue(databaseName, conversationId, ByteString(step1.clientFinalMessage))

        context.become(stepN(2, connection, databaseName, conversationId, step1))
        connection ! Write(command.encode)
      }
  }

  private def stepN(n: Int, connection: ActorRef, databaseName: String, conversationId: Int, step1: Step1): Receive = {
    case Received(data) =>
      for {
        reply <- Reply(data.asByteBuffer)
        result <- reply.documents.headOption
      } result.get[BsonValueNumber]("ok") match {
        case Some(number) if number.toInt == 0 =>
          log.error(result.getAs[String]("errmsg").getOrElse("Authentication failed."))

        case _ =>
          result.getAs[Boolean]("done") match {
            case Some(true) =>
              authenticated(connection)

            case Some(false) if n == 2 =>
              val payload = result.get[Binary]("payload").get
              val command = SaslContinue(databaseName, conversationId, computeStep2(payload.value, step1))
              context.become(stepN(n + 1, connection, databaseName, conversationId, step1))
              connection ! Write(command.encode)

            case _ =>
              log.error("Too many steps involved in the SCRAM-SHA-1 negotiation.")
          }
      }
  }

}
