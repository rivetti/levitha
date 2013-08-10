package org.opensplice.mobile.dev.group.eventual

import java.util.UUID
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.mutable.MutableList
import org.omg.dds.sub.InstanceState
import org.omg.dds.sub.Sample
import org.omg.dds.sub.SampleState
import org.omg.dds.sub.ViewState
import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.common.NULLEPOCH
import org.opensplice.mobile.dev.common.UUID2uuid
import org.opensplice.mobile.dev.dadds.WriteWrapper
import org.opensplice.mobile.dev.common.uuid2UUID
import org.opensplice.mobile.dev.dadds.DataReader
import org.opensplice.mobile.dev.dadds.DataWriter
import org.opensplice.mobile.dev.dadds.Durability.TransientLocal
import org.opensplice.mobile.dev.dadds.History.KeepAll
import org.opensplice.mobile.dev.dadds.History.KeepLast
import org.opensplice.mobile.dev.dadds.Reliability.Reliable
import org.opensplice.mobile.dev.dadds.WriterDataLifecycle
import org.opensplice.mobile.dev.dadds.Selector
import org.opensplice.mobile.dev.dadds.Topic
import org.opensplice.mobile.dev.gen.TEventualGroup
import org.opensplice.mobile.dev.group.Group
import org.opensplice.mobile.dev.group.event.JoinedGroupEvent
import org.opensplice.mobile.dev.group.event.LeftGroupEvent
import org.opensplice.mobile.dev.group.stable.TestHashSet
/*import akka.actor.Actor
import akka.actor.ActorDSL
import akka.actor.actorRef2Scala*/
import org.opensplice.mobile.dev.dadds.DataAvailable
import org.opensplice.mobile.dev.dadds.SubscriptionMatched
import org.opensplice.mobile.dev.dadds.prelude.ReaderListener
/*import akka.actor.ActorSystem
import akka.actor.ScalaActorRef*/
import scala.collection.mutable.HashSet

import org.opensplice.mobile.dev.common.DAActor

object EventualGroup {
  val EVENTUAL_GROUP_ID: String = "EventualGroup"

  def apply(group: DDSIdentifier) = new EventualGroup(DDSIdentifier(EVENTUAL_GROUP_ID, group))
}

class EventualGroup(group: DDSIdentifier)
  extends Group(EventualGroup.EVENTUAL_GROUP_ID, group) {

  // Creating Actor
  val actor = new EventualGroupMembershipActor()

  /*
   * DDS
   */

  // Eventual Group Topic
  val topic = Topic[TEventualGroup]()

  // Eventual Group Data Reader

  // Reliable so that it will retry until all reader are notified, TransientLocal to math writer QoS. KeepAll since
  // we are using the DDS cache as datasructure to keep track of group view
  val reader = DataReader[TEventualGroup](List(instanceId),
    topic,
    Reliable(),
    TransientLocal(),
    KeepAll)

  val readerListener: PartialFunction[Any, Unit] = {
    case e: DataAvailable[_] => {
      logger.trace("DataAvailable from DDS")
      eventHandler()
    }

    case e: SubscriptionMatched[_] => {
      logger.trace("SubscriptionMatched from DDS")
      eventHandler()
    }
  }

  // Selectors for the Event Handler
  // ALIVE because we are only interested in sample from alive writers, NEW and NOT_READ since we are only interested in
  // reading new samples of new instances (each writer writes on a single instance)
  val joinSelector = Selector(List(InstanceState.ALIVE), List(ViewState.NEW), List(SampleState.NOT_READ), reader)
  // NOT_ALIVE_DISPOSED and NOT_ALIVE_NO_WRITERS because we are only interested in the samples related to instances that have been
  // disposed, and/or which writers have died
  val leftSelector = Selector(List(InstanceState.NOT_ALIVE_DISPOSED, InstanceState.NOT_ALIVE_NO_WRITERS),
    null, null, reader)

  // Selectors for Size Query
  // ALIVE because we are only interested in sample from alive writers, we want to read any sample and instance state
  private val sizeSelector = Selector(List(InstanceState.ALIVE), null, null, reader)

  // Selectors for View Query
  // ALIVE because we are only interested in sample from alive writers, we want to read any sample and instance state  
  private val viewSelector = Selector(List(InstanceState.ALIVE), null, null, reader)

  // Event handler
  def eventHandler() {
    logger.trace("eventHandler")
    // TODO Uncomment for testing Olivier
    //logger.debug("READ")
    // We read on the join selector since we use the DDS cache as data structure
    val joinIterator: Sample.Iterator[TEventualGroup] = joinSelector.read()
    //logger.debug("END READ")

    //logger.debug("TAKE")
    // We take on the leave selector, since we only need to notify left members, and clean the cache
    val leftIterator: Sample.Iterator[TEventualGroup] = leftSelector.take()
    //logger.debug("END TAKE")

    actor ! new Wrapper(joinIterator, leftIterator)
  }

  // Eventual Group Data Writer
  // Reliable so that it will retry until all reader are notified, TransientLocal and KeepLast(1) so that any late joiner will
  // be notified
  def writerFactory() = DataWriter[TEventualGroup](List(instanceId),
    topic,
    Reliable(),
    TransientLocal(),
    KeepLast(1),
    WriterDataLifecycle.AutoDisposeUnregisteredInstances(false))

  val writer = WriteWrapper(writerFactory)

  def writeJoin(memberdId: UUID) {
    logger.trace("writeJoin")
    val message = new TEventualGroup(memberdId)
    writer.write(message)
  }

  def writeLeave(memberdId: UUID) {
    logger.trace("writeLeave")
    val instance = writer.lookupInstance(new TEventualGroup(memberdId))
    if (instance.isDefined) {
      //writer.dispose(instance.get)
      writer.unregisterInstance(instance.get)
    }

  }

  reader.setListener(readerListener)
  reader.enable()
  writer.create()
  Thread.sleep(500);

  /*
   * LOGIC & ACTOR
   */

  private case class Wrapper(val joinList: Iterator[Sample[TEventualGroup]], val leftList: Iterator[Sample[TEventualGroup]])

  class EventualGroupMembershipActor extends DAActor {

    def manage(wrapper: Wrapper) = {

      logger.trace("manage")
      var joinedMembers = HashSet[UUID]()
      wrapper.joinList.foreach(next => {
        val data = next.getData()
        if (data != null) {
          logger.trace("Data %s State %s".format(data.memberId.toString(), next.getInstanceState().toString()))
          joinedMembers += data.memberId
          //val event = new JoinedGroupEvent(instanceId, NULLEPOCH, data.memberId)
          //events(event)
        } else {
          logger.warn("join Null Sample")
        }
      })

      var leftMembers = HashSet[UUID]()
      wrapper.leftList.foreach(next => {
        val data = next.getData()
        if (data != null) {
          logger.trace("Data %s State %s".format(data.memberId.toString(), next.getInstanceState().toString()))
          leftMembers += data.memberId;
          //val event = new LeftGroupEvent(instanceId, NULLEPOCH, data.memberId)
          //events(event)
        } else {
          logger.warn("Left Null Sample")
        }
      })

      logger.trace("Joined: %s".format(joinedMembers.toString))
      if (!joinedMembers.isEmpty) {
        val event = new JoinedGroupEvent(instanceId, NULLEPOCH, joinedMembers)
        events(event)
      }

      logger.trace("Left: %s".format(leftMembers.toString))
      if (!leftMembers.isEmpty) {
        val event = new LeftGroupEvent(instanceId, NULLEPOCH, leftMembers)
        events(event)
      }

    }

    override var receive: PartialFunction[Any, Unit] = {
      case data: Wrapper => {
        // val start = System.nanoTime();
        manage(data)
        //println("Evantual Manage micro: %d".format((System.nanoTime() - start) / 1000))
      }
      case e: Any => { logger.warn("Unmanaged Event:" + e.toString) }
      case _ => { logger.warn("Unknown Event") }
    }
  }

  /*
   * Group Membership Abstract methods Implementation
   */

  override def join() {
    //val start = System.nanoTime();
    logger.trace("join")

    // TODO work-around for missed messages when checking for joins also in onSubscriptionMatched Olivier
    //Thread.sleep(1000)
    writeJoin(actorId)
    // println("Evantual Join micro: %d".format((System.nanoTime() - start) / 1000))
  }

  override def leave() {
    // val start = System.nanoTime();
    logger.trace("leave")
    writeLeave(actorId)
    //writer.close()
    // println("Evantual Leave micro: %d".format((System.nanoTime() - start) / 1000))
  }

  // TODO e se tenessi un contatore ?
  override def size(): Int = {
    logger.trace("size")
    sizeSelector.read().length
  }

  override def view(): (TestHashSet, Int) = {
    //val start = System.nanoTime();
    logger.trace("view")
    val it: Sample.Iterator[TEventualGroup] = viewSelector.read()
    val viewList = new TestHashSet()

    // TODO Qualcosa di meglio?
    it.foreach(item => {
      if (item.getData != null) {
        viewList.add(item.getData().memberId)
      } else {
        logger.warn("Null Sample")
      }
    })
    // println("Evantual View micro: %d".format((System.nanoTime() - start) / 1000))
    (viewList, NULLEPOCH)
  }

  override def close() {
    //actorSystem.stop(actor)
    reader.setListener(null)
    reader.close()
    writer.close()
  }
} 