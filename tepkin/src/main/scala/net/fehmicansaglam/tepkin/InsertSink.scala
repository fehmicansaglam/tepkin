package net.fehmicansaglam.tepkin

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, MaxInFlightRequestStrategy, RequestStrategy}
import net.fehmicansaglam.bson.{BsonDocument, Bulk}
import net.fehmicansaglam.tepkin.protocol.WriteConcern
import net.fehmicansaglam.tepkin.protocol.command.Insert
import net.fehmicansaglam.tepkin.protocol.message.Reply

class InsertSink(databaseName: String,
                 collectionName: String,
                 pool: ActorRef,
                 parallelism: Int,
                 ordered: Option[Boolean],
                 writeConcern: Option[BsonDocument])
  extends ActorSubscriber with ActorLogging {

  var requests = 0

  override protected def requestStrategy: RequestStrategy = new MaxInFlightRequestStrategy(parallelism) {
    override def inFlightInternally: Int = requests
  }

  override def receive: Receive = {
    case OnNext(bulk: Bulk) =>
      pool ! Insert(databaseName, collectionName, bulk.documents, ordered, writeConcern)
      requests += 1

    case reply: Reply =>
      requests -= 1
      if (requests == 0 && canceled) {
        context.stop(self)
      }
  }
}


object InsertSink {
  def props(databaseName: String,
            collectionName: String,
            pool: ActorRef,
            parallelism: Int = 1,
            ordered: Option[Boolean] = None,
            writeConcern: Option[WriteConcern] = None): Props = {
    Props(new InsertSink(databaseName, collectionName, pool, parallelism, ordered, writeConcern.map(_.toDoc)))
  }
}
