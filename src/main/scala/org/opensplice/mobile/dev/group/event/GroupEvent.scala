package org.opensplice.mobile.dev.group.event

import java.util.UUID
import org.opensplice.mobile.dev.common.DAbstractionEvent
import scala.collection.mutable.MutableList

abstract class GroupEvent( val groupId: String, val epoch: Int, val memberIds: TraversableOnce[UUID] ) extends DAbstractionEvent {
}