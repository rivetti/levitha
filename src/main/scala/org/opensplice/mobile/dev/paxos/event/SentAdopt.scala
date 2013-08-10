package org.opensplice.mobile.dev.paxos.event

import java.util.UUID

case class SentAdopt(override val instanceId: String, key: Long, sn: Int, proposer: UUID, epoch: Int) extends PaxosEvent(instanceId) {
  override def toString(): String = {
    "[%d] Sent Adopt with serial number %d, proposer id %s and epoch %d".format(key, sn, toShortString(proposer), epoch)
  }
}