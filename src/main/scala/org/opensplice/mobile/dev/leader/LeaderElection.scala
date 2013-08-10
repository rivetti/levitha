package org.opensplice.mobile.dev.leader

import java.util.UUID

import org.opensplice.mobile.dev.common.DDSDAbstraction
import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.group.Group

abstract class LeaderElection(name: String, identifier: DDSIdentifier)
    extends DDSDAbstraction(name, identifier) {
  val group: Group
  def getLeader(): Option[(UUID, Int)]
  def getEpoch(): Int
}