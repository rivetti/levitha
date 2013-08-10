package org.opensplice.mobile.dev.dadds

import org.omg.dds.core.InstanceHandle
import org.omg.dds.pub.{DataWriter => DDSDataWriter}

object WriteWrapper {
  def apply[T](writerFactory: () => DDSDataWriter[T]) = new WriteWrapper(writerFactory)
}

class WriteWrapper[T](writerFactory: () => DDSDataWriter[T]) {

  var writerWrapper: Option[DDSDataWriter[T]] = None

  def create() {
    if (writerWrapper.isEmpty) {
      val writer = writerFactory()
      writer.enable()
      writerWrapper = Some(writer)
    }
  }

  def close() {
    if (writerWrapper.isDefined) {
      writerWrapper.get.close()
      writerWrapper = None
    }
  }

  def write(sample: T) {
    if (writerWrapper.isDefined) {
      writerWrapper.get.write(sample)
    }
  }

  def lookupInstance(sample: T): Option[InstanceHandle] = {
    if (writerWrapper.isDefined) {
      Some(writerWrapper.get.lookupInstance(sample))
    } else {
      None
    }
  }

  def dispose(handle: InstanceHandle) {
    if (writerWrapper.isDefined) {
      writerWrapper.get.dispose(handle)
    }
  }

  def unregisterInstance(handle: InstanceHandle) {
    if (writerWrapper.isDefined) {
      writerWrapper.get.unregisterInstance(handle)
    }
  }

}