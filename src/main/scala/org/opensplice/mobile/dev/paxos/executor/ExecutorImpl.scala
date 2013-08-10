package org.opensplice.mobile.dev.paxos.executor

import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.paxos.{ StatePaxosData, TupleSpace, WirePaxosData }
import org.opensplice.mobile.dev.paxos.event.ReceivedDecide

object ExecutorImpl {

  val PAXOS_EXECUTOR_ID = "PAXOS:EXECUTOR"

  def apply(identifier: DDSIdentifier, wire2State: WirePaxosData => StatePaxosData) = {
    new ExecutorImpl(DDSIdentifier(PAXOS_EXECUTOR_ID, identifier), wire2State)
  }

}

class ExecutorImpl(identifier: DDSIdentifier, wire2State: WirePaxosData => StatePaxosData)
  extends Executor(ExecutorImpl.PAXOS_EXECUTOR_ID, identifier) {

  /*val probes = new Array[MessageProbe](10000)
  var count = 0
  var printed = false*/

  /*
   * Decide
   */

  TupleSpace.addListenerDecide(sample => {
    if (sample != null) {

      events(ReceivedDecide(instanceId, actorId, sample.statekey, sample.op, sample.epoch, Some(wire2State(sample.value))))

    }
  })

  def close() {
    TupleSpace.removeAllListeners()
    TupleSpace.closeSpace()
  }

}
