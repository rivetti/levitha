package org.opensplice.mobile.dev.main.paxos

import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.main.Manager
import org.opensplice.mobile.dev.paxos.client.ClientImpl
import org.opensplice.mobile.dev.paxos.executor.ExecutorImpl
import org.opensplice.mobile.dev.tools.DALogger
import org.opensplice.mobile.dev.paxos.event.ReceivedRejectedEpoch
import org.opensplice.mobile.dev.paxos.event.ReceivedDecide
import java.util.Calendar
import org.opensplice.mobile.dev.paxos.TestData

object PaxosTestTps {
  val SENDPROPOSAL = "proposal"
}

class PaxosTestTps(identifier: DDSIdentifier, warmup: Int, testIterations: Int) extends Manager with DALogger {

  import ClientManager._
  var currentEpoch = 0
  var testStarted = false
  var testEnded = false
  var startTimeMilli = 0L
  var startTimeNano = 0L
  var endTimeMilli = 0L
  var endTimeNano = 0L
  var startEpoch = 0
  var endEpoch = 0

  val key = identifier.actorId.getLeastSignificantBits()

  println("########## TEST DATA ##########")
  println("Warmup: %d - Test Iteration: %d".format(warmup, testIterations))

  val client = ClientImpl(identifier, TestData.state2Wire)
  client.events += {
    case ReceivedRejectedEpoch(_, k, e) => {
      if (k == key) {
        logger.error("Received rejected epoch %d".format(e))
        if (currentEpoch < e)
          currentEpoch = e + 1
        client.write(identifier.actorId, key, currentEpoch + 1, new TestData((currentEpoch + 1).toString))
      }
    }
  }

  val executor = ExecutorImpl(identifier, TestData.wire2State)
  executor.events += {
    case ReceivedDecide(_, _, k, op, e, v) => {

      if (k == key) {
        logger.info("Received decide key: %d,  epoch %d, value: %s".format(k, e, v))
        if (!testStarted && currentEpoch >= warmup) {
          testStarted = true
          startTimeMilli = System.currentTimeMillis()
          startTimeNano = System.nanoTime()
          startEpoch = currentEpoch

          println("########## TEST START ##########")

        }

        if (!testEnded && currentEpoch >= testIterations + warmup) {
          testEnded = true
          endTimeNano = System.nanoTime()
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

          val elapsedNano = (endTimeNano - startTimeNano)
          val elapsedMilli = elapsedNano / 1000000

          println("Elapsed Time in millisecond: %d".format(elapsedMilli))

          val tps: Double = (effectiveTestIterations.asInstanceOf[Double] / elapsedMilli.asInstanceOf[Double]) * 1000

          println("TPS: %f".format(tps))

        }

        if (!testEnded && currentEpoch < e) {
          if (currentEpoch % 10000 == 0) {
            println("Reached Epoch %d".format(currentEpoch))
          }
          currentEpoch = e
          client.write(identifier.actorId, key, currentEpoch + 1, new TestData((currentEpoch + 1).toString))
        }
      }
    }

  }

  println("########## TEST WARMUP ##########")
  client.write(identifier.actorId, key, currentEpoch + 1, new TestData((currentEpoch + 1).toString))

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
      case Nil => {}

      case _ => {
      }
    }
  }
}