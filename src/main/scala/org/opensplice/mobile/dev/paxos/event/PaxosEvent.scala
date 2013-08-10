package org.opensplice.mobile.dev.paxos.event
import org.opensplice.mobile.dev.common.DAbstractionEvent

abstract class PaxosEvent(val instanceId: String) extends DAbstractionEvent {
}