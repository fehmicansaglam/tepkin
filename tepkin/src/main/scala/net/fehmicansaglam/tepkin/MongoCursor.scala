package net.fehmicansaglam.tepkin

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.tepkin.TepkinMessage.{CursorClosed, CursorOpened, Fetch}
import net.fehmicansaglam.tepkin.protocol.message._

/**
 * Represents a cursor to read data from Mongo. This actor is an ActorPublisher so it can be passed directly to a
 * [[akka.stream.scaladsl.Source]]
 *
 * @param pool reference to a [[MongoPoolManager]] actor.
 * @param message The first message to be sent to a MongoDB instance. The cursor will be opened after this message.
 * @param extractor Extractor function for extracting ns, cursorID and firstBatch from the reply to the message.
 * @param batchMultiplier totalDemand is multiplied by this factor to calculate numberToReturn to be sent in a GetMore
 *                        message. If you are sure that your Sink is fast then try increasing this number to improve
 *                        performance.
 * @param timeout Timeout for receiving a reply for the first message.
 */
class MongoCursor(pool: ActorRef,
                  message: Message,
                  extractor: Reply => (String, Long, List[BsonDocument]), // ns, cursorID, firstBatch
                  batchMultiplier: Int,
                  timeout: Timeout)
  extends ActorPublisher[List[BsonDocument]]
  with ActorLogging {

  import context.dispatcher

  pool.?(message)(timeout).pipeTo(self).onFailure {
    case cause: Throwable => onErrorThenStop(cause)
  }

  override def receive: Receive = {
    case reply: Reply =>
      val (ns, cursorID, firstBatch) = extractor(reply)

      if (cursorID == 0 && firstBatch.isEmpty) {
        onCompleteThenStop()
      } else {
        // Notify pool manager about the opened cursor.
        if (cursorID != 0) {
          pool ! CursorOpened(cursorID)
        }

        if (totalDemand <= 0) {
          context.become(sleeping(ns, cursorID, firstBatch))
        } else if (reply.cursorID == 0) {
          onNext(firstBatch)
          onCompleteThenStop()
        } else {
          onNext(firstBatch)
          if (totalDemand > 0) {
            context.become(fetching(ns, cursorID))
            self ! Fetch
          } else {
            context.become(sleeping(ns, cursorID, Nil))
          }
        }
      }
  }

  def sleeping(ns: String, cursorID: Long, buffer: List[BsonDocument]): Receive = {
    case request@Request(_) =>
      log.debug("Received {}", request)

      if (buffer.nonEmpty) {
        onNext(buffer)
      }

      if (cursorID == 0) {
        onCompleteThenStop()
      } else {
        if (totalDemand > 0) {
          context.become(fetching(ns, cursorID))
          self ! Fetch
        } else {
          context.become(sleeping(ns, cursorID, Nil))
        }
      }

    case Cancel =>
      killCursor(cursorID)
  }

  def fetching(ns: String, cursorID: Long): Receive = {
    case reply: Reply =>
      log.debug("Received Reply. numberReturned: {} , cursorID: {}", reply.numberReturned, reply.cursorID)

      if (reply.numberReturned > 0) {
        onNext(reply.documents)
      }

      if (reply.cursorID == 0) {
        onCompleteThenStop()
        log.debug("Cursor[{}] is totally read", cursorID)
      } else if (totalDemand > 0) {
        self ! Fetch
      } else {
        context.become(sleeping(ns, cursorID, Nil))
      }

    case Fetch =>
      log.debug("Received Fetch request")
      pool ! GetMoreMessage(ns, cursorID, totalDemand.toInt * batchMultiplier)

    case Cancel =>
      killCursor(cursorID)
  }

  private def killCursor(cursorID: Long): Unit = if (cursorID != 0) {
    log.debug("Killing cursor[{}]", cursorID)
    pool ! KillCursorsMessage(cursorID)
    pool ! CursorClosed(cursorID)
    context.stop(self)
  }
}

object MongoCursor {

  /**
   *
   * @param pool reference to a MongoPool actor.
   * @param message The first message to be sent to a MongoDB instance. The cursor will be opened after this message.
   * @param extractor Extractor function for extracting ns, cursorID and firstBatch from the reply to the message.
   * @param batchMultiplier totalDemand is multiplied by this factor to calculate numberToReturn to be sent in a GetMore
   *                        message. If you are sure that your Sink is fast then try increasing this number to improve
   *                        performance.
   * @param timeout Timeout for receiving a reply for the first message.
   */
  def props(pool: ActorRef,
            message: Message,
            extractor: Reply => (String, Long, List[BsonDocument]), // ns, cursorID, firstBatch
            batchMultiplier: Int,
            timeout: Timeout): Props = {
    Props(new MongoCursor(pool, message, extractor, batchMultiplier, timeout))
  }
}
