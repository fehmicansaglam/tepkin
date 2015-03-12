package net.fehmicansaglam.tepkin

import scala.concurrent.duration._

trait RetryStrategy {
  def maxRetries: Int

  def nextDelay: Int => FiniteDuration
}

object RetryStrategy {

  case class FixedRetryStrategy(maxRetries: Int = 5,
                                delay: FiniteDuration = 500.millis) extends RetryStrategy {
    override val nextDelay: Int => FiniteDuration = _ => delay
  }

  case class LinearRetryStrategy(maxRetries: Int = 5,
                                 delay: FiniteDuration = 500.millis) extends RetryStrategy {
    override val nextDelay: Int => FiniteDuration = retry => retry * delay
  }

  case class QuadraticRetryStrategy(maxRetries: Int = 5,
                                    delay: FiniteDuration = 500.millis) extends RetryStrategy {
    override val nextDelay: Int => FiniteDuration = retry => retry * retry * delay
  }

}
