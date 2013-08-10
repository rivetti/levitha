package org.opensplice.mobile.dev.leader

import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.group.eventual.EventualGroup

object EventualLeaderElection {
  val EVENTUAL_LEADER_ID: String = "EventualLeader"

  object Default {
    import scala.language.implicitConversions
    implicit def defaultFactory(group: DDSIdentifier) = EventualGroup(group)
  }

  def apply(group: DDSIdentifier)(implicit factory: (DDSIdentifier) => EventualGroup) =
    new EventualLeaderElection(DDSIdentifier(EVENTUAL_LEADER_ID, group), factory)
}

class EventualLeaderElection(group: DDSIdentifier, factory: (DDSIdentifier) => EventualGroup)
  extends LeaderElectionImpl(EventualLeaderElection.EVENTUAL_LEADER_ID, group, factory)