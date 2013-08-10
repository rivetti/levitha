package org.opensplice.mobile.dev.paxos.event

import java.util.UUID

case class ReceivedRejectedEpoch(override val instanceId: String, key: Long, epoch: Int) extends PaxosEvent(instanceId) {
  override def toString(): String = {
    "[%d] Received Rejected Epoch with  epoch %d".format(key, epoch)
  }
}