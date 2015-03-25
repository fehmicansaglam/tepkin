package net.fehmicansaglam.tepkin

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.TepkinMessage.Fetch
import net.fehmicansaglam.tepkin.protocol.message.{GetMoreMessage, KillCursorsMessage, Reply}

class MongoCursor(pool: ActorRef,
                  fullCollectionName: String,
                  cursorID: Long,
                  initial: List[BsonDocument],
                  batchMultiplier: Int)
  extends ActorPublisher[List[BsonDocument]]
  with ActorLogging {

  override def receive: Receive = {
    case request@Request(demand) =>
      log.debug("Received {}", request)
      onNext(initial)
      if (cursorID == 0) {
        onComplete()
        context.stop(self)
      } else {
        context.become(fetching)
        self ! Fetch
      }

    case Cancel =>
      killCursor()
  }

  def sleeping: Receive = {
    case request@Request(demand) =>
      log.debug("Received {}", request)
      context become fetching
      self ! Fetch

    case Cancel =>
      killCursor()
  }

  def fetching: Receive = {
    case reply: Reply =>
      log.debug("Received Reply. numberReturned: {} , cursorID: {}", reply.numberReturned, reply.cursorID)
      onNext(reply.documents)
      if (reply.cursorID == 0) {
        onComplete()
        log.debug("Cursor[{}] is totally read", cursorID)
        killCursor()
      } else if (totalDemand > 0) {
        self ! Fetch
      } else {
        context.become(sleeping)
      }

    case Fetch =>
      log.debug("Received Fetch request")
      pool ! GetMoreMessage(fullCollectionName, cursorID, totalDemand.toInt * batchMultiplier)

    case Cancel =>
      killCursor()
  }

  private def killCursor(): Unit = {
    log.debug("Killing cursor[{}]", cursorID)
    pool ! KillCursorsMessage(cursorID)
    context.stop(self)
  }
}

object MongoCursor {
  def props(pool: ActorRef,
            fullCollectionName: String,
            cursorID: Long,
            initial: List[BsonDocument],
            batchMultiplier: Int): Props = {
    Props(classOf[MongoCursor], pool, fullCollectionName, cursorID, initial, batchMultiplier)
  }
}
