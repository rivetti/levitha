package org.opensplice.mobile.dev.paxos.event

import java.util.UUID
case class ReceivedAccepted(override val instanceId: String, key: Long, acceptor: UUID, sn: Int, proposer: UUID, epoch: Int) extends PaxosEvent(instanceId) {
  override def toString(): String = {
    "[%d] Received Accepted from %s with serial number %d, proposer id %s, epoch %d".format(key, toShortString(acceptor), sn, toShortString(proposer), epoch)
  }
}