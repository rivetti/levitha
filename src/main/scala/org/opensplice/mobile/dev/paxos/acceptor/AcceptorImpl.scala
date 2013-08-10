package org.opensplice.mobile.dev.paxos.acceptor

import java.util.UUID
import scala.collection.JavaConversions.asScalaIterator
import scala.collection.mutable.{ HashMap, SynchronizedMap }
import akka.actor.{ Actor, ActorDSL, ScalaActorRef, actorRef2Scala, scala2ActorRef }
import org.opensplice.mobile.dev.common.{ DDSIdentifier, UUID2Uuid, Uuid2UUID, DAActor }
import org.opensplice.mobile.dev.paxos.{ NullStateVersion, StatePaxosData }
import org.opensplice.mobile.dev.paxos.TupleSpace
import org.opensplice.mobile.dev.paxos.PaxosPartitions.getProposerAcceptorPartition
import org.opensplice.mobile.dev.paxos.StateVersion
import org.opensplice.mobile.dev.paxos.{ Accept, Accepted, Adopt, Adopted, RejectedVersion }
import org.opensplice.mobile.dev.paxos.WirePaxosData
import org.opensplice.mobile.dev.paxos.NullWirePaxosData
import java.util.concurrent.locks.ReentrantReadWriteLock

object AcceptorImpl {
  val PAXOS_ACCEPTOR = "PAXOS:ACCEPTOR"
  def apply[T <: StatePaxosData](group: DDSIdentifier, wire2State: WirePaxosData => StatePaxosData) = {
    new AcceptorImpl(DDSIdentifier(PAXOS_ACCEPTOR, group), wire2State)
  }
}

class AcceptorImpl(identifier: DDSIdentifier, wire2State: WirePaxosData => StatePaxosData)
  extends Acceptor(AcceptorImpl.PAXOS_ACCEPTOR, identifier) {

  val keyActorMapRWLock = new ReentrantReadWriteLock()
  val keyActorMap = new HashMap[Long, DAActor]
  val actorExecutor = DAActor.getSimpleExecutor("Acceptor")

  def getActorOrCreate(key: Long): DAActor = {
    keyActorMapRWLock.readLock().lock()
    try {
      keyActorMap.get(key) match {
        case Some(v) => v
        case None => {
          keyActorMapRWLock.readLock().unlock()
          keyActorMapRWLock.writeLock().lock()
          try {
            keyActorMap.get(key) match {
              case Some(actor) => actor
              case None => {
                val actor = new AcceptorImplActor(key)
                keyActorMap.put(key, actor)
                actor
              }
            }
          } finally {
            keyActorMapRWLock.readLock().lock()
            keyActorMapRWLock.writeLock().unlock()
          }
        }
      }
    } finally {
      keyActorMapRWLock.readLock().unlock()
    }
  }

  /*
   * Adopted Pre Handling
   */

  TupleSpace.addListenerAdopt(sample => {

    def foo() {
      val actor = getActorOrCreate(sample.statekey)
      actor ! AdoptWrapper(sample)
    }

    actorExecutor.post(foo)
  })

  /*
   * Accept Pre Handling
   */

  TupleSpace.addListenerAccept(sample => {
    def foo() {
      val actor = getActorOrCreate(sample.statekey)
      actor ! AcceptWrapper(sample)
    }

    actorExecutor.post(foo)
  })

  /*
   * Logic Implementation
   */

  case class AdoptWrapper(data: Adopt)
  case class AcceptWrapper(data: Accept)

  class AcceptorImplActor(val key: Long) extends DAActor {

    def currentSN: Int = currentVersion.serialNumber
    def currentProposer: UUID = currentVersion.proposer
    def currentEpoch: Int = currentVersion.epoch

    var currentValue: WirePaxosData = NullWirePaxosData

    var currentVersion: StateVersion = NullStateVersion

    protected def sendReject(sn: Int, proposer: UUID, epoch: Int) {
      logger.trace("[%d] sendReject".format(key))
      val sample = new RejectedVersion(key, actorId, sn, proposer, epoch)

      eventSentRejected(instanceId, key, sn, proposer, epoch)
      TupleSpace.write(sample)
    }

    protected def sendAdopted(sn: Int, proposer: UUID, epoch: Int, value: WirePaxosData, previousSn: Int, previousProposer: UUID) {
      logger.trace("[%d] sendAdopted".format(key))
      val sample = new Adopted(key, actorId, sn, proposer, epoch, value, previousSn, previousProposer)

      //eventSentAdopted(instanceId, key, sn, proposer, epoch, Some(wire2State(value.get)), previousSn, previousProposer)
      TupleSpace.write(sample)
    }

    protected def sendAccepted(sn: Int, proposer: UUID, epoch: Int) {
      logger.trace("[%d] sendAccepted".format(key))
      val sample = new Accepted(key, actorId, sn, proposer, epoch)

      eventSentAccepted(instanceId, key, sn, proposer, epoch)
      TupleSpace.write(sample)
    }

    protected def processAdoptMessages(adopt: Adopt) {
      logger.trace("[%d] processAdoptMessages".format(key))

      val version = currentVersion
      val adoptProposer: UUID = adopt.proposer
      val adoptVersion = new StateVersion(adopt.serialNumber, adoptProposer, adopt.epoch)

      eventReceivedAdopt(instanceId, key, adopt.serialNumber, adoptProposer, adopt.epoch)

      if (adopt.epoch < currentEpoch) {
        // If Adopt message epoch lower than last accepted or adopted, reject message
        logger.debug("[%d] Adopt message epoch less than current, rejecting".format(key))
        sendReject(currentSN, currentProposer, currentEpoch)
      } else if (adoptVersion <= version) {
        logger.debug("[%d] Adopt message version less or equal than current, rejecting".format(key))
        // If Adopt message version lower than or equal to last accepted or adopted, reject message          
        sendReject(currentSN, currentProposer, currentEpoch)
      } else {
        // Otherwise, adopt the Adopt message version
        logger.debug("[%d] Adopt message version greater than current, updating and answering".format(key))

        if (adopt.epoch > currentEpoch) {
          // If Adopt epoch greater than current one, reset the last accepted value
          // This is related to a previous epoch, which will no more be taken into account
          logger.debug("[%d] Adopt message epoch greater than current, resetting accepted value".format(key))
          currentValue = NullWirePaxosData

          // Send an Adopted message with null value for previously accepted value and version
          sendAdopted(adopt.serialNumber, adoptProposer, adopt.epoch, NullWirePaxosData, NullStateVersion.serialNumber, NullStateVersion.proposer)
        } else {
          sendAdopted(adopt.serialNumber, adoptProposer, adopt.epoch, currentValue, currentSN, currentProposer)
        }

        currentVersion = adoptVersion
      }

    }

    protected def processAcceptMessages(accept: Accept) {
      logger.trace("[%d] processAcceptMessages".format(key))

      val version = currentVersion

      val acceptProposer: UUID = accept.proposer
      val acceptVersion = new StateVersion(accept.serialNumber, acceptProposer, accept.epoch)

      eventReceivedAccept(instanceId, key, accept.serialNumber, acceptProposer, accept.epoch, Some(wire2State(accept.value)))

      if (accept.epoch < currentEpoch) {
        // If Accept message epoch lower than last accepted or adopted, reject message
        logger.debug("[%d] Accept message epoch less than current, rejecting".format(key))
        sendReject(currentSN, currentProposer, currentEpoch)
      } else if (acceptVersion < version) {
        // If Accept message version lower than last accepted or adopted, reject message   
        logger.debug("[%d] Accept message version less  than current, rejecting".format(key))
        sendReject(currentSN, currentProposer, currentEpoch)
      } else {
        // Otherwise, accept the Accept message value and version          
        logger.debug("[%d] Accept message epoch greater or equal than current, accepting value".format(key))
        currentValue = accept.value
        currentVersion = acceptVersion
        sendAccepted(accept.serialNumber, acceptProposer, accept.epoch)
      }

    }

    override var receive: PartialFunction[Any, Unit] = {

      // Managing messages from Adopt Topic
      case e: AdoptWrapper => {
        processAdoptMessages(e.data)
      }

      // Managing messages from Accept Topic
      case e: AcceptWrapper => {
        processAcceptMessages(e.data)
      }

      case e: Any => { logger.warn("Unmanaged Event:" + e.toString) }
      case _ => { logger.warn("Unknown Event") }
    }
  }

  def close() {
    //keyActorMap.foreach(item => { actorSystem.stop(item._2) })
  }
}