package org.opensplice.mobile.dev.paxos.event

import java.util.UUID

case class SentAccepted(override val instanceId: String, key: Long, sn: Int, proposer: UUID, epoch: Int) extends PaxosEvent(instanceId) {
  override def toString(): String = {
    "[%d] Sent Accepted with serial number %d, proposer id %s, epoch %d".format(key, sn, toShortString(proposer), epoch)
  }
}