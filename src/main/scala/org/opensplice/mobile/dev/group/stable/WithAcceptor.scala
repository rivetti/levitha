package org.opensplice.mobile.dev.group.stable

import org.opensplice.mobile.dev.paxos.acceptor.AcceptorImpl
import org.opensplice.mobile.dev.common.DDSIdentifier

object StableGroupAcceptor {
  import org.opensplice.mobile.dev.group.stable.StableGroup.STABLE_GROUP_ID
  def apply(group: DDSIdentifier) = AcceptorImpl(DDSIdentifier(STABLE_GROUP_ID, group), GroupState.wire2State)

}
