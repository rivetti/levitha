package org.opensplice.mobile.dev.paxos.proposer

import java.util.UUID

import java.util.concurrent.locks.ReentrantReadWriteLock

import scala.annotation.elidable
import scala.collection.mutable.{ HashMap, HashSet, SynchronizedMap }
import scala.language.implicitConversions

import akka.actor.{ Actor, ActorDSL, ScalaActorRef, actorRef2Scala, scala2ActorRef }

import org.opensplice.mobile.dev.common.{ DDSIdentifier, ELIDABLE_DEBUG, ELIDABLE_EVENTS, ELIDABLE_INFO, ELIDABLE_TRACE, ELIDABLE_WARN, UUID2Uuid, Uuid2UUID, DAActor }
import org.opensplice.mobile.dev.leader.EventualLeaderElection
import org.opensplice.mobile.dev.leader.EventualLeaderElection.Default.defaultFactory
import org.opensplice.mobile.dev.leader.LeaderElection
import org.opensplice.mobile.dev.leader.event.LeaderElectedEvent
import org.opensplice.mobile.dev.paxos.{ Accept, Accepted, Adopt, Adopted, Decide }
import org.opensplice.mobile.dev.paxos.{ NullStateVersion, NullWirePaxosData, PaxosOperations, Propose, RejectedEpoch, RejectedVersion, StatePaxosData, StateVersion, TupleSpace, WirePaxosData }
import org.opensplice.mobile.dev.paxos.Epoch.AnyEpoch

import nuvo.concurrent.synchronizers

object EpochState extends Enumeration {
  type EpochState = Value
  val NoState, Phase1, Phase2 = Value
}

object ProposerImpl {

  val PAXOS_PROPOSER_ID = "PAXOS:PROPOSER"
  object Default {
    import scala.language.implicitConversions
    import EventualLeaderElection.Default.defaultFactory
    implicit def leaderFactory(identifier: DDSIdentifier): LeaderElection = EventualLeaderElection(identifier)
    implicit val defaultSteadyThreshold = SteadyStateThreshold(5)
    implicit val defaultAcceptorQuorum = AcceptorQuorum(1)
  }

  def apply(identifier: DDSIdentifier, state2Wire: StatePaxosData => WirePaxosData, wire2State: WirePaxosData => StatePaxosData)(implicit leaderFactory: (DDSIdentifier) => LeaderElection,
    accQuorum: AcceptorQuorum, steadyThreshold: SteadyStateThreshold) =
    new ProposerImpl(DDSIdentifier(PAXOS_PROPOSER_ID, identifier), leaderFactory, accQuorum, steadyThreshold, state2Wire, wire2State)

  def apply(identifier: DDSIdentifier, state2Wire: StatePaxosData => WirePaxosData, wire2State: WirePaxosData => StatePaxosData, accQuorum: Int)(implicit leaderFactory: (DDSIdentifier) => LeaderElection,
    steadyThreshold: SteadyStateThreshold) =
    new ProposerImpl(DDSIdentifier(PAXOS_PROPOSER_ID, identifier), leaderFactory, AcceptorQuorum(accQuorum), steadyThreshold, state2Wire, wire2State)

}

case class SteadyStateThreshold(value: Int)
case class AcceptorQuorum(value: Int)

class ProposerImpl(identifier: DDSIdentifier, leaderFactory: (DDSIdentifier) => LeaderElection,
  accQuorum: AcceptorQuorum, steadyThreshold: SteadyStateThreshold, state2Wire: StatePaxosData => WirePaxosData, wire2State: WirePaxosData => StatePaxosData)
  extends Proposer(ProposerImpl.PAXOS_PROPOSER_ID, identifier) {

  val keyActorMapRWLock = new ReentrantReadWriteLock()
  val keyActorMap = new HashMap[Long, DAActor]
  val actorExecutor = DAActor.getExecutor("Proposer")

  /*
   * PROPOSAL
   */
  TupleSpace.createLocalSpaceServer
  TupleSpace.createLocalSpaceServerAcceptor
  TupleSpace.addListenerPropose(sample => {

    def foo() {

      val actor = keyActorMap.getOrElseUpdate(sample.statekey, {
        logger.info("[%d] Creating Actor".format(sample.statekey))
        new ProposerImplActor(sample.statekey)
      });
      actor ! ProposeWrapper(sample)
    }

    actorExecutor.post(foo)

  })

  /*
   * ADOPTED
   */

  TupleSpace.addListenerAdopted(sample => {

    def foo() {
      val actor = keyActorMap.getOrElseUpdate(sample.statekey, {
        logger.info("[%d] Creating Actor".format(sample.statekey))
        new ProposerImplActor(sample.statekey)
      });
      actor ! AdoptedWrapper(sample)
    }

    actorExecutor.post(foo)
  })

  /*
   * ACCEPTED
   */

  TupleSpace.addListenerAccepted(sample => {
    def foo() {
      val actor = keyActorMap.getOrElseUpdate(sample.statekey, {
        logger.info("[%d] Creating Actor".format(sample.statekey))
        new ProposerImplActor(sample.statekey)
      });
      actor ! AcceptedWrapper(sample)
    }

    actorExecutor.post(foo)
  })

  /*
   * REJECTED
   */

  TupleSpace.addListenerRejectedVersion(sample => {
    def foo() {
      val actor = keyActorMap.getOrElseUpdate(sample.statekey, {
        logger.info("[%d] Creating Actor".format(sample.statekey))
        new ProposerImplActor(sample.statekey)
      });
      actor ! RecjectedWrapper(sample)
    }

    actorExecutor.post(foo)
  })

  def createReaders() {

  }

  def closeReaders() {

  }

  def createWriters() {

  }

  def closeWriters() {

  }

  // Manage Leadership

  var leader = false;

  //TODO all'inizio non ci sta nessun attore! Devo gestire qui la leadership
  val leaderElection = leaderFactory(identifier)
  leaderElection.events += {
    case e: LeaderElectedEvent => {

      this.synchronized(

        if (!leader && e.leaderId.equals(actorId)) {
          logger.debug("%s is  transient leader".format(toShortString(actorId)))
          createReaders()
          createWriters()
          leader = true
        } else if (leader && !e.leaderId.equals(actorId)) {
          logger.debug("%s is  no more leader".format(toShortString(actorId)))
          leader = false
          //keyActorMap.foreach(item => { actorSystem.stop(item._2) })
          closeReaders()
          closeWriters()
        })

    }
  }

  leaderElection.group.join()

  /*
   * Actor Implementation
   */

  private case class AdoptedWrapper(data: Adopted)
  private case class AcceptedWrapper(data: Accepted)
  private case class RecjectedWrapper(data: RejectedVersion)
  private case class ProposeWrapper(data: Propose)

  private case class StartRound()

  class ProposerImplActor(val key: Long) extends DAActor {

    import EpochState._

    var lastDecidedValue: Option[StatePaxosData] = None
    var lastDecidedEpoch: Int = -1

    var majorProposalValues = List[(Int, StatePaxosData)]()
    var addProposalValues = List[(Int, StatePaxosData)]()
    var rmvProposalValues = List[(Int, StatePaxosData)]()

    var majorNextProposalValues = List[(Int, StatePaxosData)]()
    var addNextProposalValues = List[(Int, StatePaxosData)]()
    var rmvNextProposalValues = List[(Int, StatePaxosData)]()

    var currentSerialNumber: Int = 0

    var currentEpoch: Int = 0

    var adoptedSet = List[UUID]()

    //var acceptedSet = List[UUID]()
    var acceptedCount = 0

    var candidateValue: Option[StatePaxosData] = None
    var candidateOperations: Int = 0

    var highestProposal: (Option[StatePaxosData], StateVersion) = (None, NullStateVersion)
    def highestProposaVersion = highestProposal._2
    def highestProposalValue = highestProposal._1

    var epochState: EpochState = NoState

    var decisionCount = 0

    val steadyStateThreshold = steadyThreshold.value

    val acceptorQuorum = accQuorum.value

    def roundReset() {
      adoptedSet = Nil
      //acceptedSet = Nil
      acceptedCount = 0
      highestProposal = (None, NullStateVersion)
      candidateValue = None
    }

    def writeRejected(epoch: Int) {
      logger.trace("[%d] writeRejected".format(key))
      val sample = new RejectedEpoch(key, epoch)

      eventSentRejectedEpoch(instanceId, key, epoch)
      TupleSpace.write(sample)
    }

    def writeAdopt(serialNumber: Int, proposer: UUID, epoch: Int) {
      logger.trace("[%d] writeAdopt".format(key))
      val sample = new Adopt(key, serialNumber, proposer, epoch)

      eventSentAdopt(instanceId, key, serialNumber, proposer, epoch)
      TupleSpace.write(sample)
    }

    def writeAccept(serialNumber: Int, proposer: UUID, epoch: Int, value: Option[StatePaxosData]) {
      logger.trace("[%d] writeAccept".format(key))
      val sample = Accept(key, serialNumber, proposer, epoch, state2Wire(value.get))
      eventSentAccept(instanceId, key, serialNumber, proposer, epoch, value)
      TupleSpace.write(sample)
    }

    def writeDecide(op: Int, epoch: Int, value: Option[StatePaxosData]) {
      logger.trace("[%d] writeDecide".format(key))

      val sample = new Decide(key, op, epoch, state2Wire(value.get))

      eventSentDecide(instanceId, key, op, epoch, value)

      TupleSpace.write(sample)
    }

    override var receive: PartialFunction[Any, Unit] = transientLeader

    /*
     * TRANSIENT & STEADY COMMON METHODS
     */
    var batched = 0
    //var clients = new HashSet[Long]()
    def managePropose(proposal: Propose, startNextRound: () => Unit) = {
      logger.trace("[%d] managePropose".format(key))

      val proposalValue: Option[StatePaxosData] = Some(wire2State(proposal.value))
      eventReceivedProposal(instanceId, proposal.client, key, proposal.op, proposal.epoch, proposalValue)

      if (proposal.op == PaxosOperations.READ) {
        writeDecide(PaxosOperations.READ, lastDecidedEpoch, lastDecidedValue)
      } else if (proposal.epoch == AnyEpoch()) {

        /*if (!clients.contains(proposal.clientL))
          clients.add(proposal.clientL)*/

        if (epochState == NoState) {
          // If nothing is running, start it
          logger.debug("[%d] proposal with any epoch and current epoch %d is not runnig, adding and starting".format(key, currentEpoch))

          if (proposal.op <= PaxosOperations.WRITE_OPS_HIGH_LIMIT) {
            majorProposalValues = (proposal.op, proposalValue.get) :: majorProposalValues
          } else if (proposal.op <= PaxosOperations.ADD) {
            addProposalValues = (proposal.op, proposalValue.get) :: addProposalValues
          } else if (proposal.op <= PaxosOperations.REMOVE) {
            rmvProposalValues = (proposal.op, proposalValue.get) :: rmvProposalValues
          }

          logger.debug("[%d] major proposalValues:".format(key) + majorProposalValues.mkString("|"))
          logger.debug("[%d] add proposalValues:".format(key) + addProposalValues.mkString("|"))
          logger.debug("[%d] rmv proposalValues:".format(key) + rmvProposalValues.mkString("|"))

          //if (addProposalValues.size + rmvProposalValues.size >= 10)
          startNextRound()

          // Start Protocol
        } else if (epochState == Phase1) {
          logger.debug("[%d] proposal with any epoch and current epoch %d in phase 1, adding".format(key, currentEpoch))

          if (proposal.op <= PaxosOperations.WRITE_OPS_HIGH_LIMIT) {
            majorProposalValues = (proposal.op, proposalValue.get) :: majorProposalValues
          } else if (proposal.op <= PaxosOperations.ADD) {
            addProposalValues = (proposal.op, proposalValue.get) :: addProposalValues
          } else if (proposal.op <= PaxosOperations.REMOVE) {
            rmvProposalValues = (proposal.op, proposalValue.get) :: rmvProposalValues
          }

          logger.debug("[%d] major proposalValues:".format(key) + majorProposalValues.mkString("|"))
          logger.debug("[%d] add proposalValues:".format(key) + addProposalValues.mkString("|"))
          logger.debug("[%d] rmv proposalValues:".format(key) + rmvProposalValues.mkString("|"))
        } else {
          batched += 1
          logger.debug("[%d] proposal with any epoch and current epoch %d in running, adding to next proposal set".format(key, currentEpoch))

          if (proposal.op <= PaxosOperations.WRITE_OPS_HIGH_LIMIT) {
            majorNextProposalValues = (proposal.op, proposalValue.get) :: majorNextProposalValues
          } else if (proposal.op <= PaxosOperations.ADD) {
            addNextProposalValues = (proposal.op, proposalValue.get) :: addNextProposalValues
          } else if (proposal.op <= PaxosOperations.REMOVE) {
            rmvNextProposalValues = (proposal.op, proposalValue.get) :: rmvNextProposalValues
          }

          logger.debug("[%d] major next proposalValues:".format(key) + majorNextProposalValues.mkString("|"))
          logger.debug("[%d] add next proposalValues:".format(key) + addNextProposalValues.mkString("|"))
          logger.debug("[%d] rmv next proposalValues:".format(key) + rmvNextProposalValues.mkString("|"))
        }

        if (currentEpoch % 10000 == 0)
          println("Batched: %d".format(batched))

      } else if (proposal.epoch < currentEpoch) {
        // Received a proposal with epoch less than current one, sending a rejected message
        logger.debug("[%d] proposal epoch %d less than current epoch %d, rejecting it".format(key, proposal.epoch, currentEpoch))
        writeRejected(currentEpoch)

      } else if (proposal.epoch > currentEpoch) {
        // Received a proposal with epoch greater than current one. We move ahead to this epoch, aborting the eventually running one. 
        // Dropping all previous epoch data

        majorProposalValues = Nil
        addProposalValues = Nil
        rmvProposalValues = Nil
        if (proposal.op <= PaxosOperations.WRITE_OPS_HIGH_LIMIT) {
          majorProposalValues = (proposal.op, proposalValue.get) :: majorProposalValues
        } else if (proposal.op <= PaxosOperations.ADD) {
          addProposalValues = (proposal.op, proposalValue.get) :: addProposalValues
        } else if (proposal.op <= PaxosOperations.REMOVE) {
          rmvProposalValues = (proposal.op, proposalValue.get) :: rmvProposalValues
        }
        currentEpoch = proposal.epoch

        logger.debug("[%d] major proposalValues:".format(key) + majorProposalValues.mkString("|"))
        logger.debug("[%d] add proposalValues:".format(key) + addProposalValues.mkString("|"))
        logger.debug("[%d] rmv proposalValues:".format(key) + rmvProposalValues.mkString("|"))

        startNextRound()

      } else {
        // Received a proposal with epoch equal to current one.
        logger.debug("[%d] proposal epoch %d equal tocurrent epoch %d, adding".format(key, proposal.epoch, currentEpoch))
        if (epochState != Phase2) {
          // If the current epoch is not in phase2, then add the new value
          majorProposalValues = (proposal.op, proposalValue.get) :: majorProposalValues
          // If the current epoch was not ready, now it is
          if (epochState == NoState) {
            startNextRound()
          }

          logger.debug("[%d] proposalValues:".format(key) + majorProposalValues.mkString("|"))
        }
      }

    }

    def manageRejected(rejected: RejectedVersion): Boolean = {
      logger.trace("[%d] manageRejected".format(key))
      var greaterRejected = false;

      eventReceivedRejected(instanceId, key, rejected.acceptor, rejected.serialNumber, rejected.proposer, rejected.epoch)

      def rejectedSnDiff(): Int = {
        val snDiff = rejected.serialNumber - currentSerialNumber
        if (snDiff != 0) {
          return snDiff
        } else {
          return rejected.proposer.compareTo(actorId)
        }
      }
      val rejectedSnAndIdGreaterOrEqualThan = rejectedSnDiff >= 0;

      if (rejected.epoch > currentEpoch) {
        // If the Rejected Epoch message is greater than the current one, then we can abort the current epoch, 
        //move to new one, notify the clients and reset the decision count

        decisionCount = 0

        currentEpoch = rejected.epoch
        epochState = NoState
        roundReset()

        writeRejected(currentEpoch)

        if (rejectedSnAndIdGreaterOrEqualThan) {
          // Also, if the upper version is greater, update it
          currentSerialNumber = rejected.serialNumber
        }

      } else if (rejectedSnAndIdGreaterOrEqualThan) {
        // If the epoch is equal to the current one, but the upper version is greater or equal to the current one,
        // then update the upper version, reset the decision count and check if a round should be restarted
        logger.info("[%d] rejected (sn, prop) %d,%s greater than current (sn,prop) %d,%s, moving ahead".format(key, rejected.serialNumber, rejected.proposer, currentSerialNumber, actorId))

        decisionCount = 0
        currentSerialNumber = rejected.serialNumber + 1
        roundReset()

        if (epochState != NoState) {
          // If the epoch was running, restart it
          epochState = Phase1
          writeAdopt(currentSerialNumber, actorId, currentEpoch)
        }

      }

      // If the epoch is less than the current on, ignore it

      //Chech for stead leader
      if (rejectedSnAndIdGreaterOrEqualThan || rejected.epoch > currentEpoch) {
        greaterRejected = greaterRejected || true;
      }

      return greaterRejected;
    }

    def manageAccepted(accepted: Accepted, startNextRound: () => Unit) = {
      logger.trace("[%d] manageAccepted".format(key))

      val acceptorUUID: UUID = accepted.acceptor
      val proposerUUID: UUID = accepted.proposer

      eventReceivedAccepted(instanceId, key, acceptorUUID,
        accepted.serialNumber, proposerUUID, accepted.epoch)

      if (accepted.epoch == currentEpoch &&
        accepted.serialNumber == currentSerialNumber &&
        proposerUUID == actorId &&
        epochState == Phase2) {
        // If Accepted message is for current epoch with current upper version, and the current epoch is in phase 2,
        // then add it to the accepted set.        

        acceptedCount += 1
        //acceptedSet = acceptedSet.+:(acceptorUUID)

        logger.debug("[%d] Accepted Message added to acceptedSet, current size = %d".format(key, acceptedCount)) // acceptedSet.size))
      }

      if ( /*acceptedSet.size*/ acceptedCount >= acceptorQuorum) {
        // If the quorum is reached, take the decision, reset the round data, move to following epoch, increase the decision count
        // and drop epoch data
        logger.debug("[%d] Accepted Quorum (%d) reached".format(key, acceptorQuorum))
        writeDecide(candidateOperations, currentEpoch, candidateValue)

        lastDecidedValue = candidateValue
        lastDecidedEpoch = currentEpoch

        roundReset()

        currentEpoch += 1
        decisionCount += 1
        epochState = NoState
        majorProposalValues = Nil
        addProposalValues = Nil
        rmvProposalValues = Nil

        // TODO ocio al cambio tra transient e steady!
        if (!majorNextProposalValues.isEmpty || !addNextProposalValues.isEmpty || !rmvNextProposalValues.isEmpty) {
          majorProposalValues = majorNextProposalValues
          addProposalValues = addNextProposalValues
          rmvProposalValues = rmvNextProposalValues
          majorNextProposalValues = Nil
          addNextProposalValues = Nil
          rmvNextProposalValues = Nil
          startNextRound()
        }

      }

    }

    def computeCandidateValue() {
      logger.trace("[%d] computeCandidateValue".format(key))

      if (!majorProposalValues.isEmpty) {
        val majorProposal = majorProposalValues.head
        logger.debug("[%d] Major Ops".format(key))
        majorProposal._1 match {
          case PaxosOperations.WRITE => {
            logger.debug("[%d] WRITE".format(key))
            candidateValue = Some(majorProposal._2)
            candidateOperations = PaxosOperations.WRITE
          }

          case PaxosOperations.WRITE_OWNER => {
            logger.debug("[%d] WRITE_OWNER".format(key))
            candidateValue = Some(majorProposal._2)
            candidateOperations = PaxosOperations.WRITE_OWNER
            // OwnerShip
          }

          case PaxosOperations.TAKE => {
            logger.debug("[%d] TAKE".format(key))
            candidateValue = None
            candidateOperations = PaxosOperations.TAKE
          }
          case _ => {
            logger.warn("Unkown Op");
          }
        }
      } else {
        logger.debug("[%d] Minor Ops (%s)".format(key, lastDecidedValue.toString()))
        // TODO non devo modificare il vecchi valore deciso, ci vorrebbe una clone
        if (lastDecidedValue.isDefined) {
          candidateValue = Some(lastDecidedValue.get.update(addProposalValues, rmvProposalValues))
        } else if (!addProposalValues.isEmpty) {
          candidateValue = Some(addProposalValues.head._2.update(addProposalValues.tail, rmvProposalValues))
        }

        if (!addProposalValues.isEmpty)
          candidateOperations = candidateOperations | PaxosOperations.ADD

        if (!rmvProposalValues.isEmpty)
          candidateOperations = candidateOperations | PaxosOperations.REMOVE

      }
      logger.debug("[%d] Candidate Value: %s".format(key, candidateValue.toString()))
    }

    /*
     * TRANSIENT LEADER METHODS
     */

    def tstartNextRound() {

      epochState = Phase1
      currentSerialNumber += 1
      /* majorProposalValues ++= majorNextProposalValues
      addProposalValues ++= addNextProposalValues
      rmvProposalValues ++= rmvNextProposalValues*/
      /* majorNextProposalValues = Nil
      addNextProposalValues = Nil
      rmvNextProposalValues = Nil*/
      roundReset()
      writeAdopt(currentSerialNumber, actorId, currentEpoch)
    }

    def tLeaderManagePropose(data: Propose) = {
      logger.trace("[%d] tLeaderManagePropose".format(key))

      managePropose(data, tstartNextRound)
    }

    def tLeaderManageRejected(data: RejectedVersion) = {
      logger.trace("[%d] LeaderManageRejected".format(key))
      manageRejected(data)
    }

    def tLeaderManageAdopted(adopted: Adopted) = {
      logger.trace("[%d] tLeaderManageAdopted".format(key))

      if (adopted.epoch == currentEpoch &&
        adopted.serialNumber == currentSerialNumber &&
        Uuid2UUID(adopted.proposer) == actorId &&
        epochState == Phase1) {
        // If Adopted message is for current epoch with current upper version, and the current epoch is in phase 1,
        // Then add it to the adopted set and check if the value has a higher version with respect to the stored one

        val adoptedValue: Option[StatePaxosData] = adopted.value match {
          case e: NullWirePaxosData.type => None
          case e: WirePaxosData => Some(wire2State(e))
          case _ => None
        }

        eventReceivedAdopted(instanceId, key, adopted.acceptor, adopted.serialNumber, adopted.proposer, adopted.epoch,
          adoptedValue, adopted.previousSerialNumber, adopted.previousProposer)

        adoptedSet = adoptedSet.+:(Uuid2UUID(adopted.acceptor))

        logger.debug("[%d] Adopted Message added to adoptedSet, current size = %d".format(key, adoptedSet.size))

        val previousVersion = new StateVersion(adopted.previousSerialNumber, adopted.previousProposer, adopted.epoch)

        if (adoptedValue.isDefined && previousVersion > highestProposal._2) {
          // If the Adopted message carries a valid value, and it's acceptance version is higher than the acceptance version of the stored value

          logger.debug("[%d] Message value has a higher versioned (%d, %s) value than last stored (%d, %s), swapping".
            format(key, adopted.serialNumber, Uuid2UUID(adopted.proposer).toString, highestProposal._2.serialNumber, highestProposal._2.proposer.toString))

          logger.debug("[%d] Pre:".format(key) + (highestProposal._1 + ", " + highestProposal._2))
          highestProposal = (adoptedValue, previousVersion)
          logger.debug("[%d] Post:".format(key) + highestProposal._1 + ", " + highestProposal._2)
        }

        if (adoptedSet.size >= acceptorQuorum) {
          // If the quorum is reached, reset the adopted set, move to phase 2 and select a candidate value
          logger.debug("[%d] Adopted Quorum (%d) reached".format(key, acceptorQuorum))
          adoptedSet = Nil
          epochState = Phase2

          if (highestProposal._1.isDefined) {
            // If a value has been stored, then used it
            logger.debug("[%d] Adopted values where not all null, setting highest one as candidate value".format(key))
            candidateValue = highestProposal._1
          } else {
            // Otherwise select one from the proposed values
            logger.debug("[%d] Adopted values where all null, setting a proposal value as candidate value".format(key))
            computeCandidateValue()
          }
          writeAccept(currentSerialNumber, actorId, currentEpoch, candidateValue)
        }

      }

    }

    def tLeaderManageAccepted(data: Accepted) = {
      logger.trace("[%d] tLeaderManageAccepted".format(key))
      // Manage accepted returns true if a decisione has been taken
      manageAccepted(data, tstartNextRound)
      if (decisionCount >= steadyStateThreshold) {
        println("[%d] GOING TO STEADY".format(key))
        logger.debug("[%d] %s moves from TRANSIENT to STEADY".format(key, toShortString(actorId)))
        become(steadyLeader)
      }

    }

    def transientLeader: PartialFunction[Any, Unit] = {

      case e: ProposeWrapper => tLeaderManagePropose(e.data)

      case e: RecjectedWrapper => tLeaderManageRejected(e.data)

      case e: AdoptedWrapper => tLeaderManageAdopted(e.data)

      case e: AcceptedWrapper => {
        tLeaderManageAccepted(e.data)
      }

      case _ => {}
    }

    /*
     * STEADY LEADER METHODS
     */

    //val scheduled = new org.opensplice.mobile.dev.common.DAActor.DAActorScheduleTask(foo)

    def foo() {
      epochState = Phase2
      roundReset()
      computeCandidateValue()
      writeAccept(currentSerialNumber, actorId, currentEpoch, candidateValue)
    }

    def sstartNextRound() {
      actorExecutor.schedule(foo)
    }

    def sLeaderManagePropose(data: Propose) = {
      logger.trace("[%d] sLeaderManagePropose".format(key))

      managePropose(data, sstartNextRound)
    }

    def sLeaderManageRejected(data: RejectedVersion) = {
      logger.trace("[%d] sLeaderManageRejected".format(key))

      // ManageRejected returns true if a rejected message had version greater or equal than current, or epoch greater  or equal than current
      if (manageRejected(data)) {
        decisionCount = 0
        println("[%d] GOING TO TRANSIENT".format(key))
        logger.debug("[%d] %s moves from STEADY to TRANSIENT".format(key, toShortString(actorId)))
        become(transientLeader)
      }

    }

    def sLeaderManageAccepted(data: Accepted) = {
      logger.trace("[%d] sLeaderManageAccepted".format(key))
      manageAccepted(data, sstartNextRound)
    }

    def steadyLeader: PartialFunction[Any, Unit] = {

      case e: ProposeWrapper => {
        sLeaderManagePropose(e.data)
      }

      case e: RecjectedWrapper => {
        sLeaderManageRejected(e.data)
      }

      case e: AcceptedWrapper => {
        sLeaderManageAccepted(e.data)
      }

      case _ => {}
    }
  }

  def close() {
    //keyActorMap.foreach(item => { actorSystem.stop(item._2) })

  }

}
