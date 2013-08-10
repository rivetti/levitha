package org.opensplice.mobile.dev.paxos.client

import java.util.UUID

import scala.annotation.elidable

import org.opensplice.mobile.dev.common.{ DDSIdentifier, ELIDABLE_EVENTS, ELIDABLE_TRACE, UUID2Uuid }
import org.opensplice.mobile.dev.paxos.{ NullWirePaxosData, PaxosOperations }
import org.opensplice.mobile.dev.paxos.{ Propose, StatePaxosData, TupleSpace, WirePaxosData }
import org.opensplice.mobile.dev.paxos.Epoch.{ AnyEpoch, anyEpoch2Long }
import org.opensplice.mobile.dev.paxos.PaxosPartitions.getClientProposerPartition
import org.opensplice.mobile.dev.paxos.event.SentProposal

object ClientImpl {

  val PAXOS_CLIENT_ID = "PAXOS:CLIENT"

  def apply(identifier: DDSIdentifier, state2Wire: StatePaxosData => WirePaxosData) = {
    new ClientImpl(DDSIdentifier(PAXOS_CLIENT_ID, identifier), state2Wire)
  }

}

class ClientImpl(identifier: DDSIdentifier, state2Wire: StatePaxosData => WirePaxosData)
  extends Client(ClientImpl.PAXOS_CLIENT_ID, identifier) {

  val partition = getClientProposerPartition(identifier)

  /*
   * Rejected Epoch
   */
  TupleSpace.addListenerRejectedEpoch(sample => {
    eventReceivedRejectedEpoch(instanceId, sample.statekey, sample.epoch)
  })

  override def write(cid: UUID, key: Long, epoch: Int, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.WRITE, epoch, value)
  }
  override def write(cid: UUID, key: Long, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.WRITE, AnyEpoch, value)
  }

  override def write_owner(cid: UUID, key: Long, epoch: Int, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.WRITE_OWNER, epoch, value)
  }
  override def write_owner(cid: UUID, key: Long, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.WRITE_OWNER, AnyEpoch, value)
  }

  override def take(cid: UUID, key: Long, epoch: Int) {
    sendProposal(cid, key, PaxosOperations.TAKE, epoch)
  }
  override def take(cid: UUID, key: Long) {
    sendProposal(cid, key, PaxosOperations.TAKE, AnyEpoch)
  }

  override def read(cid: UUID, key: Long) {
    sendProposal(cid, key, PaxosOperations.READ, AnyEpoch)
  }

  override def read_last(cid: UUID, key: Long) {
    sendProposal(cid, key, PaxosOperations.READ_LAST, AnyEpoch)
  }

  override def add(cid: UUID, key: Long, epoch: Int, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.ADD, epoch, value)
  }
  override def add(cid: UUID, key: Long, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.ADD, AnyEpoch, value)
  }

  override def add_owner(cid: UUID, key: Long, epoch: Int, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.ADD_OWNER, epoch, value)
  }
  override def add_owner(cid: UUID, key: Long, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.ADD_OWNER, AnyEpoch, value)
  }

  override def remove(cid: UUID, key: Long, epoch: Int, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.REMOVE, epoch, value)
  }
  override def remove(cid: UUID, key: Long, value: StatePaxosData) {
    sendProposal(cid, key, PaxosOperations.REMOVE, AnyEpoch, value)
  }

  private def sendProposal(cid: UUID, key: Long, op: Int, epoch: Int, proposal: StatePaxosData) {
    logger.trace("sendProposal")
    TupleSpace.write(new Propose(cid, key, op, epoch, state2Wire(proposal)))
    events(SentProposal(instanceId, cid, key, op, epoch, Some(proposal)))
  }

  private def sendProposal(cid: UUID, key: Long, op: Int, epoch: Int) {
    logger.trace("sendProposal")
    TupleSpace.write(new Propose(cid, key, op, epoch, NullWirePaxosData))
    events(SentProposal(instanceId, cid, key, op, epoch, None))
  }

  def close() {
    TupleSpace.removeAllListeners()
    TupleSpace.closeSpace()
  }

}
