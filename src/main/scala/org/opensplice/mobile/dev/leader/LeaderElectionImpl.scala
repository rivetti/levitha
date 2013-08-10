package org.opensplice.mobile.dev.leader

import java.util.UUID
import java.util.concurrent.atomic.{ AtomicInteger, AtomicReference }

//import akka.actor.{Actor, ActorDSL, ScalaActorRef, actorRef2Scala, scala2ActorRef}

import org.opensplice.mobile.dev.common.DAActor
import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.group.Group
import org.opensplice.mobile.dev.group.event.{ JoinedGroupEvent, LeftGroupEvent }
import org.opensplice.mobile.dev.leader.event.LeaderElectedEvent

abstract class LeaderElectionImpl(name: String, identifier: DDSIdentifier, factory: (DDSIdentifier) => Group)
  extends LeaderElection(name, identifier) {

  val actor = new LeaderElectionActor()

  val currentLeader = new AtomicReference[Option[Tuple2[UUID, Int]]](None)
  val currentEpoch = new AtomicInteger(0)

  override val group = factory(identifier)

  group.events += {
    case e: JoinedGroupEvent => {
      logger.trace("JoinedGroupEvent")
      actor ! e
    }
    case e: LeftGroupEvent => {
      logger.trace("LeftGroupEvent")
      actor ! e
    }
  }

  /*
   * Actor Implementation
   */

  class LeaderElectionActor extends DAActor {

    private def electLeader() {
      logger.trace("electLeader")
      def selectLeader(): UUID = {
        val memberList = group.view()._1
        if (memberList.isEmpty) {
          identifier.actorId
        } else {
          val sortedList = memberList.toList.sortWith((elem1, elem2) => elem1.compareTo(elem2) > 0)
          sortedList(0)
        }
      }

      val candidate = selectLeader()
      val newEpoch = currentEpoch.incrementAndGet()
      val previousLeaderWrapper = currentLeader.getAndSet(Option((candidate, newEpoch)))
      val event = new LeaderElectedEvent(identifier.instanceId, newEpoch, candidate)
      events(event)
    }

    override var receive: PartialFunction[Any, Unit] = {
      case joined: JoinedGroupEvent => {
        logger.trace("JoinedGroupEvent")
        electLeader()
      }

      case left: LeftGroupEvent => {
        logger.trace("LeftGroupEvent")
        electLeader()
      }

      case e: Any => { logger.warn("Unmanaged Event:" + e.toString) }
      case _ => { logger.warn("Unknown Event") }

    }
  }

  /*
   * Leader Election Abstract methods Implementation
   */

  override def getLeader(): Option[(UUID, Int)] = currentLeader.get
  override def getEpoch(): Int = currentEpoch.get

  override def close() {
    //actorSystem.stop(actor)
    group.close()
  }

}