package net.fehmicansaglam.tepkin

import scala.concurrent.duration._

trait RetryStrategy {
  def maxRetries: Int

  def nextDelay: Int => FiniteDuration
}

object RetryStrategy {

  case class FixedDelay(maxRetries: Int = 5,
                        delay: FiniteDuration = 500.millis) extends RetryStrategy {
    override val nextDelay: Int => FiniteDuration = _ => delay
  }

}
