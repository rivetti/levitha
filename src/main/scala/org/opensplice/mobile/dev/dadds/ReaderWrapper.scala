package org.opensplice.mobile.dev.dadds

import java.util.ArrayList
import scala.annotation.elidable
import org.omg.dds.sub.Sample
import org.opensplice.mobile.dev.common.{ ELIDABLE_TRACE, ELIDABLE_WARN }
import org.opensplice.mobile.dev.dadds.prelude.ReaderListener
import org.opensplice.mobile.dev.tools.DALogger
import org.omg.dds.sub.DataReader.{ Selector => DDSSelector }
import org.omg.dds.sub.{DataReader => DDSDataReader}

object ReaderWrapper {
  def apply[T](writerFactory: () => DDSDataReader[T]) = new ReaderWrapper(writerFactory)
}

class ReaderWrapper[T](readerFactory: () => DDSDataReader[T]) extends DALogger {
  var readerWrapper: Option[DDSDataReader[T]] = None

  def create() {
    logger.trace("ReaderWrapper create")
    if (readerWrapper.isEmpty) {
      readerWrapper = Some(readerFactory())
    }
  }

  def close() {
    logger.trace("ReaderWrapper close")
    if (readerWrapper.isDefined) {
      readerWrapper.get.setListener(null)
      readerWrapper.get.close()
      readerWrapper = None
    }
  }

  def read(): Option[Sample.Iterator[T]] = {
    logger.trace("ReaderWrapper read")
    if (readerWrapper.isDefined) {
      Some(readerWrapper.get.read())
    } else {
      None
    }
  }

  def take(): Option[Sample.Iterator[T]] = {
    logger.trace("ReaderWrapper take")
    if (readerWrapper.isDefined) {
      Some(readerWrapper.get.take())
    } else {
      None
    }
  }

  def setListener(listener: PartialFunction[Any, Unit]) {
    if (readerWrapper.isDefined) {
      Some(readerWrapper.get.setListener(listener))
    }
  }

  def enable() {
    if (readerWrapper.isDefined) {
      readerWrapper.get.enable()
    }
  }

  object SelectorWrapper {
    def apply(selectorFactory: (DDSDataReader[T]) => DDSSelector[T]) =
      new SelectorWrapper(selectorFactory)
  }

  class SelectorWrapper(selectorFactory: (DDSDataReader[T]) => DDSSelector[T]) {
    var selectorWrapper: Option[DDSSelector[T]] = None

    def create() {
      logger.trace("SelectorWrapper create")
      if (readerWrapper.isDefined && selectorWrapper.isEmpty)
        selectorWrapper = Some(selectorFactory(readerWrapper.get))
    }

    def read(): Sample.Iterator[T] = {
      logger.trace("SelectorWrapper read")
      if (selectorWrapper.isDefined) {
        selectorWrapper.get.read()
      } else {
        logger.warn("########### NULL!")
        null
      }
    }

    def read(list: ArrayList[T]) {
      logger.trace("SelectorWrapper read")
      if (selectorWrapper.isDefined) {
        selectorWrapper.get.read()
      } else {
        logger.warn("########### NULL!")
      }
    }

    def take(): Sample.Iterator[T] = {
      logger.trace("SelectorWrapper take")
      if (selectorWrapper.isDefined) {
        selectorWrapper.get.take()
      } else {
        logger.warn("########### NULL!")
        null
      }
    }

  }

}