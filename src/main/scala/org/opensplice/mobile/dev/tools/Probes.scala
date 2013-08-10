package org.opensplice.mobile.dev.tools

import java.io.PrintWriter
import java.io.File

object Probes {
  def apply() = new Probes(50000, 50000, "data")
  def apply(warmup: Int, test: Int) = new Probes(warmup, test, "data")
  def apply(fileName: String) = new Probes(50000, 50000, fileName)
  def apply(warmup: Int, test: Int, fileName: String) = new Probes(warmup, test, fileName)
}

class Probes(warmup: Int, test: Int, fileName: String) {

  var startTime = 0L
  val latencies = new Array[Long](test)
  var warmupCounter = 0
  var testCounter = 0
  var started = false
  var ended = false

  def start() {
    if (warmupCounter >= warmup) {
      started = true
    }

    if (started && !ended) {
      startTime = System.nanoTime();
    } else {
      warmupCounter += 1
    }
  }

  def end() {
    if (started && !ended) {
      latencies(testCounter) = System.nanoTime() - startTime
      testCounter += 1
      if (testCounter >= test)
        ended = true
    }
  }

  def print() {

    val writer = new PrintWriter(fileName)
    for (i <- 0 to testCounter - 1) {
      writer.print("%d\n".format(latencies(i)))
    }
    writer.flush()
    writer.close()
  }
}

object GlobalAutoPrintProbe1 {
  val probe = AutoPrintProbes(999, 1000, 1000, "GlobalProbe1")
  def start() { probe.start }
  def end() { probe.end }
}

object AutoPrintProbes {
  def apply() = new AutoPrintProbes(50000, 50000, 50000, "data")
  def apply(warmup: Int, size: Int) = new AutoPrintProbes(size, warmup, size, "data")
  def apply(threshold: Int, warmup: Int, size: Int) = new AutoPrintProbes(threshold, warmup, size, "data")
  def apply(fileName: String) = new AutoPrintProbes(50000, 50000, 50000, fileName)
  def apply(warmup: Int, size: Int, fileName: String) = new AutoPrintProbes(size, warmup, size, fileName)
  def apply(threshold: Int, warmup: Int, size: Int, fileName: String) = new AutoPrintProbes(threshold, warmup, size, fileName)
}

class AutoPrintProbes(threshold: Int, warmup: Int, size: Int, fileName: String) extends Probes(warmup, size, fileName) {
  var printed = false

  override def end() {
    super.end()

    if (testCounter >= threshold && !printed) {
      printed = true
      print()
    }

  }
}

