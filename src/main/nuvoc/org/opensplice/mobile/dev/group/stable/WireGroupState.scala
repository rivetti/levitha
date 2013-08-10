package org.opensplice.mobile.dev.group.stable

import org.opensplice.mobile.dev.paxos.WirePaxosData
import scala.collection.immutable.SortedSet
import java.util.UUID

case class WireGroupState(members: SortedSet[UUID]) extends WirePaxosData {
  lazy val key = ()
}
