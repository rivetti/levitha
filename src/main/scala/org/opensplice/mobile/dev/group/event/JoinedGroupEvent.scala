package org.opensplice.mobile.dev.group.event

import java.util.UUID
import scala.collection.mutable.MutableList
import scala.collection.IterableLike

case class JoinedGroupEvent(override val groupId: String, override val epoch: Int, override val memberIds: TraversableOnce[UUID])
    extends GroupEvent(groupId, epoch, memberIds) {
  override def toString: String = {
   
    
    "Member %s has JOINED Group %s in epoch %s".format(memberIds.mkString(","), groupId, epoch)
  }
}