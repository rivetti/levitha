package org.opensplice.mobile.dev.tools

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent

class ThreadIdConverter extends ClassicConverter {
  override def convert(event: ILoggingEvent): String = "%03d".format(Thread.currentThread.getId)
}