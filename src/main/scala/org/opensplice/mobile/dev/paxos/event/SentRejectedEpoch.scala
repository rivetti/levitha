package org.opensplice.mobile.dev.paxos.event

import java.util.UUID

case class SentRejectedEpoch(override val instanceId: String, key: Long, epoch: Int) extends PaxosEvent(instanceId) {
  override def toString(): String = {
    "[%d] Sent Rejected Epoch with  epoch %d".format(key, epoch)
  }
}