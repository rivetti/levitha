package org.opensplice.mobile.dev.paxos.event

import java.util.UUID
import org.opensplice.mobile.dev.paxos.StatePaxosData

case class ReceivedAccept(override val instanceId: String, key: Long, sn: Int, proposer: UUID, epoch: Int, value: Option[StatePaxosData]) extends PaxosEvent(instanceId) {
  override def toString(): String = {
    "[%d] Received Accept with serial number %d, proposer id %s, epoch %d and value %s".format(key, sn, toShortString(proposer), epoch, value.toString)
  }
}