package org.opensplice.mobile.dev.group.stable

import org.opensplice.mobile.dev.paxos.WirePaxosData
import org.opensplice.mobile.dev.paxos.StatePaxosData
import java.util.UUID
import org.opensplice.mobile.dev.paxos.PaxosExceptions.{ AddOwnerOperationNotSupported, RemoveOwnerOperationNotSupported }
import org.opensplice.mobile.dev.common.Uuid
import scala.collection.immutable.SortedSet

object GroupState {
  def state2Wire(value: StatePaxosData) = {
    new WireGroupState(value.asInstanceOf[GroupState].state).asInstanceOf[WirePaxosData]
  }

  def wire2State(value: WirePaxosData) = {
    new GroupState(value.asInstanceOf[WireGroupState].members)
  }
}

case class GroupState(var state: SortedSet[UUID]) extends StatePaxosData {
  override def toString(): String = {
    "GroupState: %s".format(state.toString)
  }

  override def hashCode(): Int = {
    state.hashCode()
  }

  override def equals(any: Any): Boolean = {
    if (!any.isInstanceOf[GroupState])
      return false

    val that = any.asInstanceOf[GroupState]

    if (this.state.size == that.state.size && this.state.hashCode() == that.state.hashCode())
      return true

    return false
  }

  override def add(value: StatePaxosData): StatePaxosData = {
    this
  }

  override def remove(value: StatePaxosData): StatePaxosData = {
    this
  }

  override def update(add: List[(Int, StatePaxosData)], rmv: List[(Int, StatePaxosData)]) = {
    if (!state.isEmpty && !rmv.isEmpty) {
      rmv.foreach(x => state = state - x._2.asInstanceOf[GroupState].state.head)
    }

    add.foreach(x => state = state + x._2.asInstanceOf[GroupState].state.head)
    this
  }
}