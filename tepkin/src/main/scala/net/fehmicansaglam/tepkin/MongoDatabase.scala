package net.fehmicansaglam.tepkin

import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Source
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.TepkinMessage.WhatsYourVersion
import net.fehmicansaglam.tepkin.protocol.MongoWireVersion
import net.fehmicansaglam.tepkin.protocol.command.{Command, ListCollections}
import net.fehmicansaglam.tepkin.protocol.message.Reply

import scala.concurrent.{ExecutionContext, Future}

class MongoDatabase(pool: ActorRef, databaseName: String) {

  def apply(collectionName: String): MongoCollection = {
    require(collectionName != null && collectionName.getBytes("UTF-8").size < 123,
      "Collection name must be shorter than 123 bytes")
    new MongoCollection(databaseName, collectionName, pool)
  }

  def collection(collectionName: String): MongoCollection = apply(collectionName)

  def gridFs(prefix: String = "fs"): GridFs = {
    new GridFs(this, prefix)
  }

  def listCollections(filter: Option[BsonDocument] = None, batchMultiplier: Int = 1000)
                     (implicit ec: ExecutionContext, timeout: Timeout): Future[Source[List[BsonDocument], ActorRef]] = {
    (pool ? WhatsYourVersion).mapTo[Int].flatMap { maxWireVersion =>
      if (maxWireVersion == MongoWireVersion.v30) {
        (pool ? ListCollections(databaseName, filter)).mapTo[Reply].map { reply =>
          val cursor = reply.documents(0).getAs[BsonDocument]("cursor").get
          val cursorID = cursor.getAs[Long]("id").get
          val ns = cursor.getAs[String]("ns").get
          val initial = cursor.getAsList[BsonDocument]("firstBatch").get

          Source.actorPublisher(MongoCursor.props(pool, ns, cursorID, initial, batchMultiplier))
        }
      } else {
        apply("system.namespaces").find(BsonDocument.empty)
      }
    }
  }

  def runCommand(document: BsonDocument)(implicit ec: ExecutionContext, timeout: Timeout): Future[Reply] = {
    val command = new Command {

      override def command: BsonDocument = document

      override def databaseName: String = MongoDatabase.this.databaseName
    }

    (pool ? command).mapTo[Reply]
  }

}

