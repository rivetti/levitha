package org.opensplice.mobile.dev.leader

import scala.language.implicitConversions

import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.group.stable.StableGroup
import org.opensplice.mobile.dev.group.stable.StableGroup.Default.{defaultClientFactory, defaultExecutorFactory, defaultLeaderFactory}

object StableLeaderElection {
  val STABLE_LEADER_ID: String = "StableLeader"

  object Default {
    import scala.language.implicitConversions
    import StableGroup.Default._
    implicit def defaultFactory(group: DDSIdentifier) =
      StableGroup(group)

  }

  def apply(identifier: DDSIdentifier)(implicit factory: (DDSIdentifier) => StableGroup) =
    new StableLeaderElection(DDSIdentifier(STABLE_LEADER_ID, identifier), factory)
}
class StableLeaderElection(group: DDSIdentifier, factory: (DDSIdentifier) => StableGroup)
  extends LeaderElectionImpl(StableLeaderElection.STABLE_LEADER_ID, group, factory) 