package org.opensplice.mobile.dev.paxos.event

import java.util.UUID
import org.opensplice.mobile.dev.paxos.StatePaxosData

case class ReceivedDecide(override val instanceId: String, val clientId: UUID, val key: Long, val op: Int, val epoch: Int, val value: Option[StatePaxosData]) extends PaxosEvent(instanceId) {
  override def toString(): String = {
    "[%d] Received decide op %d with value %s for epoch %d".format(key, op, value.toString, epoch)
  }
}