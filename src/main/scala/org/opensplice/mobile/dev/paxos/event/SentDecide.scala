package org.opensplice.mobile.dev.paxos.event

import java.util.UUID
import org.opensplice.mobile.dev.paxos.StatePaxosData

case class SentDecide(override val instanceId: String, val key: Long, val op: Int, val epoch: Int, val answer: Option[StatePaxosData]) extends PaxosEvent(instanceId) {
  override def toString(): String = {
    "[%d] Sent decide op %d with value %s for epoch %d".format(key, op, answer.toString, epoch)
  }
}