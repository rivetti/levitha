package org.opensplice.mobile.dev.leader.event

import org.opensplice.mobile.dev.common.DAbstractionEvent

abstract class LeaderElectionEvent(val groupId: String, val epoch: Int) extends DAbstractionEvent {

}