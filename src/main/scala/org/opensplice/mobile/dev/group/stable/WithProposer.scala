package org.opensplice.mobile.dev.group.stable

import org.opensplice.mobile.dev.paxos.proposer.ProposerImpl
import org.opensplice.mobile.dev.common.DDSIdentifier

object StableGroupProposer {
  import org.opensplice.mobile.dev.group.stable.StableGroup.STABLE_GROUP_ID
  import ProposerImpl.Default._

  def apply(group: DDSIdentifier) = ProposerImpl(DDSIdentifier(STABLE_GROUP_ID, group), GroupState.state2Wire, GroupState.wire2State)
  def apply(group: DDSIdentifier, acceptorQyorum: Int) = ProposerImpl(DDSIdentifier(STABLE_GROUP_ID, group), GroupState.state2Wire, GroupState.wire2State, acceptorQyorum)

}