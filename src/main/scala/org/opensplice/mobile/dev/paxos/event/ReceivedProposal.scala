package org.opensplice.mobile.dev.paxos.event

import java.util.UUID
import org.opensplice.mobile.dev.paxos.StatePaxosData

case class ReceivedProposal(override val instanceId: String, val clientId: UUID, val key: Long, val op: Int, val epoch: Int, val request: Option[StatePaxosData]) extends PaxosEvent(instanceId) {
  override def toString(): String = {

    "[%d]  Client %s has sent op %d with value %s for epoch %d".format(key, toShortString(clientId), op, request, epoch)
  }
}
