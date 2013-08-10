package org.opensplice.mobile.dev.leader.event

import java.util.UUID

case class LeaderElectedEvent(override val groupId: String, override val epoch: Int, val leaderId: UUID)
    extends LeaderElectionEvent(groupId, epoch) {

  override def toString(): String = {
    "Member " + toShortString(leaderId) + " has been ELECTED Leader of Group " + groupId + " in Epoch " + epoch
  }
}