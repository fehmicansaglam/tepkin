package net.fehmicansaglam.tepkin

import org.slf4j.{Logger => Slf4jLogger, LoggerFactory}

trait LoggerLike {

  /** The underlying SLF4J Logger.
    */
  def logger: Slf4jLogger

  /** `true` if the logger instance is enabled for the `TRACE` level.
    */
  def isTraceEnabled: Boolean = logger.isTraceEnabled

  /** `true` if the logger instance is enabled for the `DEBUG` level.
    */
  def isDebugEnabled: Boolean = logger.isDebugEnabled

  /** `true` if the logger instance is enabled for the `INFO` level.
    */
  def isInfoEnabled: Boolean = logger.isInfoEnabled

  /** `true` if the logger instance is enabled for the `WARN` level.
    */
  def isWarnEnabled: Boolean = logger.isWarnEnabled

  /** `true` if the logger instance is enabled for the `ERROR` level.
    */
  def isErrorEnabled: Boolean = logger.isErrorEnabled

  /** Logs a message with the `TRACE` level.
    *
    * @param message the message to log
    */
  def trace(message: => String): Unit = {
    if (logger.isTraceEnabled) logger.trace(message)
  }

  /** Logs a message with the `TRACE` level.
    *
    * @param message the message to log
    * @param error the associated exception
    */
  def trace(message: => String, error: => Throwable): Unit = {
    if (logger.isTraceEnabled) logger.trace(message, error)
  }

  /** Logs a message with the `DEBUG` level.
    *
    * @param message the message to log
    */
  def debug(message: => String): Unit = {
    if (logger.isDebugEnabled) logger.debug(message)
  }

  /** Logs a message with the `DEBUG` level.
    *
    * @param message the message to log
    * @param error the associated exception
    */
  def debug(message: => String, error: => Throwable): Unit = {
    if (logger.isDebugEnabled) logger.debug(message, error)
  }

  /** Logs a message with the `INFO` level.
    *
    * @param message the message to log
    */
  def info(message: => String): Unit = {
    if (logger.isInfoEnabled) logger.info(message)
  }

  /** Logs a message with the `INFO` level.
    *
    * @param message the message to log
    * @param error the associated exception
    */
  def info(message: => String, error: => Throwable): Unit = {
    if (logger.isInfoEnabled) logger.info(message, error)
  }

  /** Logs a message with the `WARN` level.
    *
    * @param message the message to log
    */
  def warn(message: => String): Unit = {
    if (logger.isWarnEnabled) logger.warn(message)
  }

  /** Logs a message with the `WARN` level.
    *
    * @param message the message to log
    * @param error the associated exception
    */
  def warn(message: => String, error: => Throwable): Unit = {
    if (logger.isWarnEnabled) logger.warn(message, error)
  }

  /** Logs a message with the `ERROR` level.
    *
    * @param message the message to log
    */
  def error(message: => String): Unit = {
    if (logger.isErrorEnabled) logger.error(message)
  }

  /** Logs a message with the `ERROR` level.
    *
    * @param message the message to log
    * @param error the associated exception
    */
  def error(message: => String, error: => Throwable): Unit = {
    if (logger.isErrorEnabled) logger.error(message, error)
  }

}

class Logger(val logger: Slf4jLogger) extends LoggerLike

object Logger extends LoggerLike {

  lazy val logger = LoggerFactory.getLogger("tepkin")

  def apply(name: String): Logger = new Logger(LoggerFactory.getLogger(name))

  def apply[T](clazz: Class[T]): Logger = new Logger(LoggerFactory.getLogger(clazz))

}
