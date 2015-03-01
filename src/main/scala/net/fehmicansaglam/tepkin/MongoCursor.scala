package net.fehmicansaglam.tepkin

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.util.Timeout
import net.fehmicansaglam.tepkin.TepkinMessages.Fetch
import net.fehmicansaglam.tepkin.bson.BsonDocument
import net.fehmicansaglam.tepkin.protocol.message.{GetMoreMessage, KillCursorsMessage, Reply}

import scala.concurrent.duration._

class MongoCursor(pool: ActorRef, fullCollectionName: String, cursorID: Long)
  extends ActorPublisher[List[BsonDocument]]
  with ActorLogging {

  implicit val timeout: Timeout = 10.seconds

  import context.dispatcher

  override def receive: Receive = {
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
        context become receive
      }

    case Fetch =>
      log.debug("Received Fetch request")
      (pool ? GetMoreMessage(fullCollectionName, cursorID, totalDemand.toInt * 10)).mapTo[Reply] pipeTo self
      ()

    case Cancel =>
      killCursor()
  }

  private def killCursor(): Unit = {
    log.debug("Killing cursor[{}]", cursorID)
    pool ! KillCursorsMessage(cursorID)
    context stop self
  }
}

object MongoCursor {
  def props(pool: ActorRef, fullCollectionName: String, cursorID: Long): Props = {
    Props(classOf[MongoCursor], pool, fullCollectionName, cursorID)
  }
}
