package org.opensplice.mobile.dev.main.paxos

import java.io.PrintWriter
import java.util.Calendar
import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.main.Manager
import org.opensplice.mobile.dev.paxos.client.ClientImpl
import org.opensplice.mobile.dev.paxos.event.ReceivedDecide
import org.opensplice.mobile.dev.paxos.event.ReceivedRejectedEpoch
import org.opensplice.mobile.dev.paxos.executor.ExecutorImpl
import org.opensplice.mobile.dev.tools.DALogger
import org.opensplice.mobile.dev.paxos.TestData
import org.opensplice.mobile.dev.paxos.Epoch._
import org.opensplice.mobile.dev.paxos.PaxosOperations

class PaxosTestLatency(identifier: DDSIdentifier, warmup: Int, testIterations: Int) extends Manager with DALogger {

  import ClientManager._

  val key = identifier.actorId.getLeastSignificantBits()

  var writeToFile = false
  var fileName = "data"

  var currentEpoch = -1
  var testStarted = false
  var testEnded = false
  var startTimeMilli = 0L
  var endTimeMilli = 0L
  var startEpoch = 0
  var endEpoch = 0

  var startTimeNano = 0L
  var endTimeNano = 0L
  var testEpoch = 0
  val latencies = new Array[Long](testIterations)

  def foo() = {}
  var function: () => Unit = foo

  var added = false

  var read = false

  println("########## TEST DATA ##########")
  println("Warmup: %d - Test Iteration: %d".format(warmup, testIterations))

  val client = ClientImpl(identifier, TestData.state2Wire)
  client.events += {
    case ReceivedRejectedEpoch(_, k, e) => {
      if (k == key) {
        println("Received rejected epoch %d".format(e))
        if (currentEpoch < e)
          currentEpoch = e + 1
      }
    }
  }

  val executor = ExecutorImpl(identifier, TestData.wire2State)
  executor.events += {
    case ReceivedDecide(_, _, k, op, e, v) => {

      if (k == key) {
        //println(v.toString())

        if (testStarted && !testEnded) {
          endTimeNano = System.nanoTime()
          latencies(testEpoch) = endTimeNano - startTimeNano
          testEpoch = testEpoch + 1
        }

        if (!testStarted && currentEpoch >= warmup) {
          testStarted = true
          startTimeMilli = System.currentTimeMillis()
          startEpoch = currentEpoch

          println("########## TEST START ##########")

        }

        if (!testEnded && currentEpoch >= testIterations + warmup) {
          testEnded = true

          endTimeMilli = System.currentTimeMillis()
          endEpoch = currentEpoch

          client.close()
          executor.close()

          println("########## TEST ENDED ##########")

          val calendar = Calendar.getInstance
          calendar.setTimeInMillis(startTimeMilli);
          println("Start Time %02d:%02d:%02d:%03d".format(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND)))
          calendar.setTimeInMillis(endTimeMilli);
          println("End Time %02d:%02d:%02d:%03d".format(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND), calendar.get(Calendar.MILLISECOND)))

          val effectiveTestIterations: Long = endEpoch - startEpoch

          println("Performed %d iterations, %d warmup and %d of tests".format(currentEpoch, startEpoch, effectiveTestIterations))

          import java.io.PrintWriter
          val writer = new PrintWriter(fileName)
          for (i <- 0 to (latencies.length - 1)) {
            // println("%04d: %d".format(i, latencies(i)))
            writer.print("%d\n".format(latencies(i)))
          }
          writer.close()

        }

        if (!read && !testEnded && currentEpoch < e) {
          if (currentEpoch % 10000 == 0) {
            println("Reached Epoch %d".format(currentEpoch))
          }

          if (testStarted && !testEnded) {
            startTimeNano = System.nanoTime()
          }

          currentEpoch = e

          function()
        } else if (read && !testEnded && !(e < currentEpoch)) {
          if (currentEpoch % 10000 == 0) {
            println("Reached Epoch %d".format(currentEpoch))
          }

          if (testStarted && !testEnded) {
            startTimeNano = System.nanoTime()
          }

          currentEpoch = currentEpoch + 1

          function()

        }
      }

    }
  }

  println("########## TEST WARMUP ##########")
  //client.write(identifier.actorId, key, currentEpoch + 1, new TestData((currentEpoch + 1).toString))

  override def manageCommand(commands: List[String]) {

    commands match {
      case CLOSE :: Nil => {

        client.close()
        executor.close()

      }

      case _ => {}
    }

  }

  override def manageArgs(args: List[String]) {
    args match {

      case "-write" :: tail => {
        println("write")
        def bar() {
          client.write(identifier.actorId, key, currentEpoch + 1, new TestData((currentEpoch + 1).toString))
        }
        function = bar
        function()
      }

      case "-read" :: tail => {
        println("read")

        def bar() {
          client.read(identifier.actorId, key)
        }
        read = true;
        function = bar
        client.write(identifier.actorId, key, 1, new TestData((1).toString))
      }

      case "-take" :: tail => {
        println("take")
        def bar() {
          client.take(identifier.actorId, key)
        }
        function = bar
        function()
      }

      case "-addrmv" :: tail => {
        println("addrmv")
        def bar() {

          if (added) {
            added = false
            client.add(identifier.actorId, key, AnyEpoch, new TestData((currentEpoch + 1).toString))
          } else {
            added = true
            client.remove(identifier.actorId, key, AnyEpoch, new TestData((currentEpoch + 1).toString))
          }

        }

        function = bar
        function()
      }

      case Nil => {}

      case _ => {
      }
    }
  }
}