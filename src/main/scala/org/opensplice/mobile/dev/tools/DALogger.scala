package org.opensplice.mobile.dev.tools

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.opensplice.mobile.dev.common.{ELIDABLE_TRACE, ELIDABLE_DEBUG, ELIDABLE_INFO, ELIDABLE_WARN, ELIDABLE_ERROR}
import scala.annotation.elidable
import scala.annotation.elidable._
trait DALogger {

  protected lazy val logger = new MyLogger(LoggerFactory.getLogger(DALogger.this.getClass()))

  protected def getStackTrace(stackTrace: Array[StackTraceElement]) = {
    var str = ""
    for (stackElem <- stackTrace)
      str += stackElem + "\n"

    str
  }

}

class MyLogger(logger: Logger) {
  @elidable(ELIDABLE_TRACE)
  def trace(string: String) {
    logger.trace(string)
  }

  @elidable(ELIDABLE_DEBUG)
  def debug(string: String) {
    logger.debug(string)
  }

  @elidable(ELIDABLE_INFO)
  def info(string: String) {
    logger.info(string)
  }

  @elidable(ELIDABLE_INFO)
  def info(format: String, string: String) {
    logger.info(format, string)
  }

  @elidable(ELIDABLE_WARN)
  def warn(string: String) {
    logger.warn(string)
  }

  @elidable(ELIDABLE_ERROR)
  def error(string: String) {
    logger.error(string)
  }

}
