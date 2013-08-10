package org.opensplice.mobile.dev.group.stable

import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable.HashSet
import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.common.NULLEPOCH
import org.opensplice.mobile.dev.common.Timer
import org.opensplice.mobile.dev.group.Group
import org.opensplice.mobile.dev.group.event.GroupEvent
import org.opensplice.mobile.dev.group.event.JoinedGroupEvent
import org.opensplice.mobile.dev.group.event.LeftGroupEvent
import org.opensplice.mobile.dev.group.eventual.EventualGroup
import org.opensplice.mobile.dev.leader.EventualLeaderElection
import org.opensplice.mobile.dev.leader.event.LeaderElectedEvent
import org.opensplice.mobile.dev.paxos.acceptor.AcceptorImpl
import org.opensplice.mobile.dev.paxos.client.ClientImpl
import org.opensplice.mobile.dev.paxos.event.ReceivedDecide
import org.opensplice.mobile.dev.paxos.event.ReceivedRejectedEpoch
import org.opensplice.mobile.dev.paxos.executor.ExecutorImpl
import org.opensplice.mobile.dev.paxos.proposer.ProposerImpl
import akka.actor.Actor
import akka.actor.ActorDSL
import akka.actor.actorRef2Scala
import org.opensplice.mobile.dev.leader.LeaderElection
import org.opensplice.mobile.dev.paxos.client.Client
import org.opensplice.mobile.dev.paxos.executor.Executor
import akka.actor.ScalaActorRef
import org.opensplice.mobile.dev.group.event.JoinedGroupEvent
import org.opensplice.mobile.dev.group.event.LeftGroupEvent
import scala.util.Random
import org.opensplice.mobile.dev.paxos.Epoch._
import org.opensplice.mobile.dev.tools.AutoPrintProbes
import scala.collection.immutable.SortedSet
import org.opensplice.mobile.dev.common.DAActor

object StableGroup {

  val STABLE_GROUP_ID: String = "StableGroup"
  val RECHECK_TIMEOUT: Int = 10000

  object Default {
    import scala.language.implicitConversions
    import org.opensplice.mobile.dev.leader.EventualLeaderElection.Default.defaultFactory
    implicit def defaultLeaderFactory(group: DDSIdentifier): LeaderElection = EventualLeaderElection(group)
    implicit def defaultClientFactory(group: DDSIdentifier): Client = ClientImpl(group, GroupState.state2Wire)
    implicit def defaultExecutorFactory(group: DDSIdentifier): Executor = ExecutorImpl(group, GroupState.wire2State)
  }

  def apply(group: DDSIdentifier)(implicit leaderFactory: (DDSIdentifier) => LeaderElection,
    clientFactory: (DDSIdentifier) => Client,
    executorFactory: (DDSIdentifier) => Executor) =
    new StableGroup(DDSIdentifier(STABLE_GROUP_ID, group), leaderFactory, clientFactory, executorFactory)
}

class StableGroup(group: DDSIdentifier, leaderFactory: (DDSIdentifier) => LeaderElection,
  clientFactory: (DDSIdentifier) => Client,
  executorFactory: (DDSIdentifier) => Executor)
  extends Group(StableGroup.STABLE_GROUP_ID, group) {

  val rand = new Random()
  for (i <- 1 to 10) {
    rand.nextLong
  }

  val key = instanceId.hashCode

  // Creating Actor
  val actor: DAActor = new StableGroupMembershipActor
  val actorExecutor = DAActor.getDummyExecutor("StableGroup")

  // Group Members Set
  val groupMembers = new AtomicReference[SortedSet[UUID]]()
  groupMembers.set(SortedSet[UUID]())
  // Group Epoch
  val currentEpoch = new AtomicInteger(NullEpoch)
  var joined = false;

  /*
   * Executor
   */

  var start = 0L

  val executor = executorFactory(group)
  executor.events += {
    case e: ReceivedDecide => {
      def foo() {
        logger.trace("ReceivedDecide %s from Executor".format(e))
        actor ! e
      }

      actorExecutor.post(foo)
    }
  }

  val client = clientFactory(group)
  client.events += {
    case e: ReceivedRejectedEpoch => {
      def foo() {
        logger.trace("ReceivedRejectedEpoch %s From Paxos Client".format(e))
        actor ! e
      }

      actorExecutor.post(foo)
    }
  }

  /*
   * Actor Implementation
   */

  private class StableGroupMembershipActor extends DAActor {

    override var receive: PartialFunction[Any, Unit] = {
      case e: ReceivedDecide => {
        //case e @ ReceivedDecide(_, _, _, _, _, _) => {

        // TODO check del tipo
        manageDecided(e.epoch, e.value.get.asInstanceOf[GroupState])

        def manageDecided(epoch: Int, data: GroupState) = {
          logger.trace("manageDecided")
          // Da mettere >= invece di >
          if (epoch > currentEpoch.get) {
            logger.debug("Received Decide Message with Epoch %d greater than current %d".format(epoch, currentEpoch.get))

            val newState: SortedSet[UUID] = data.state
            val oldState = groupMembers.get

            currentEpoch.set(epoch)
            groupMembers.set(newState)

            //println("Moving from old state: %s, to new state: %s".format(oldState, newState))
            logger.debug("Moving from old state: %s, to new state: %s".format(oldState, newState))

            if (oldState.isEmpty && newState.isEmpty) {

              logger.debug("curret state and new state empty")

            } else if (oldState.isEmpty) {

              logger.debug("current state empty, replacing with new state")
              events(new JoinedGroupEvent(instanceId.toString, currentEpoch.get, newState))

            } else if (newState.isEmpty) {

              logger.debug("new state empty, all leave")

              events(new LeftGroupEvent(instanceId.toString, currentEpoch.get, oldState))

            } else {
              logger.debug("current state and new state not empty, computing diffs")

              var left = Set[UUID]()
              var joined = Set[UUID]()

              left = oldState.diff(newState)
              joined = newState.diff(oldState)

              //println(newState.mkString("|"))
              //println(oldState.mkString("|"))

              if (!left.isEmpty)
                events(new LeftGroupEvent(instanceId.toString, currentEpoch.get, left))

              if (!joined.isEmpty)
                events(new JoinedGroupEvent(instanceId.toString, currentEpoch.get, joined))

            }

          }
        }

        //println("Stable decide micro: %d".format((System.nanoTime() - start) / 1000))
      }

      case e: ReceivedRejectedEpoch => {
        /*  // val start = System.nanoTime();
        logger.trace("ReceivedRejectedEpoch")
        // Non reagire al volo, ci pensa il timer
        if (e.epoch >= leaderCurrentEpoch) {
          // timer ! Timer.Reset
          leaderCurrentEpoch = e.epoch + 1
          //  client.sendProposal(actorId, currentEpoch.get + 1, GroupState(eventualGroup.view()._1))
        }
        //println("Stable Rejected micro: %d".format((System.nanoTime() - start) / 1000))
*/ }

      case e: Any => { logger.warn("Unmanaged Event:" + e.toString) }
      case _ => { logger.warn("Unknown Event") }
    }
  }

  /*
   * Group Membership Abstract methods Implementation
   */

  override def join() {
    logger.trace("join")
    client.add(actorId, key, AnyEpoch, new GroupState(SortedSet(actorId)))
  }

  override def leave() {
    logger.trace("leave")

    client.remove(actorId, key, AnyEpoch, new GroupState(SortedSet(actorId)))
  }

  override def view(): (SortedSet[UUID], Int) = {
    logger.trace("view")
    if (!groupMembers.get.isEmpty)
      (groupMembers.get, currentEpoch.get)
    else
      (SortedSet[UUID](), currentEpoch.get)
  }

  override def size(): Int = {
    logger.trace("size")
    if (!groupMembers.get.isEmpty)
      groupMembers.get.size
    else
      0
  }

  override def close() {
    leave()
    //actorSystem.stop(actor)
    executor.close()
    client.close()
  }
}