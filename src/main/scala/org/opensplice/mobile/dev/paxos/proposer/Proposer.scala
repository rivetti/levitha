package org.opensplice.mobile.dev.paxos.proposer

import java.util.UUID
import org.opensplice.mobile.dev.common.ELIDABLE_EVENTS
import org.opensplice.mobile.dev.common.ELIDABLE_ESPER
import org.opensplice.mobile.dev.common.DDSDAbstraction
import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.paxos.{  NullStatePaxosData, NullWirePaxosData, StatePaxosData, WirePaxosData }
import org.opensplice.mobile.dev.paxos.event.ReceivedAccepted
import org.opensplice.mobile.dev.paxos.event.ReceivedAdopted
import org.opensplice.mobile.dev.paxos.event.ReceivedProposal
import org.opensplice.mobile.dev.paxos.event.SentAccept
import org.opensplice.mobile.dev.paxos.event.SentAdopt
import org.opensplice.mobile.dev.paxos.event.SentDecide
import org.opensplice.mobile.dev.paxos.event.SentRejectedEpoch
import scala.annotation.elidable
import scala.annotation.elidable._
import org.opensplice.mobile.dev.paxos.event.ReceivedRejectedVersion
import org.opensplice.mobile.dev.paxos.event.ReceivedAccepted
import org.opensplice.mobile.dev.paxos.event.SentAdopt

abstract class Proposer(name: String, identifier: DDSIdentifier) extends DDSDAbstraction(name, identifier) {

  @elidable(ELIDABLE_EVENTS)
  def eventReceivedProposal(instanceId: String, client: UUID, key: Long, op: Int, epoch: Int, value: Option[StatePaxosData]) {
      events(ReceivedProposal(instanceId, client, key, op, epoch, value))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventSentAdopt(instanceId: String, key: Long, serialNumber: Int, proposer: UUID, epoch: Int) {
      events(SentAdopt(instanceId, key, serialNumber, proposer, epoch))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventReceivedAdopted(instanceId: String, key: Long, acceptor: UUID, serialNumber: Int, proposer: UUID, epoch: Int, adoptedValue: Option[StatePaxosData], previousSerialNumber: Int, previousProposer: UUID) {
      events(ReceivedAdopted(instanceId, key, acceptor, serialNumber, proposer, epoch,
        adoptedValue, previousSerialNumber, previousProposer))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventSentAccept(instanceId: String, key: Long, serialNumber: Int, proposer: UUID, epoch: Int, value: Option[StatePaxosData]) {
      events(SentAccept(instanceId, key, serialNumber, proposer, epoch, value))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventReceivedAccepted(instanceId: String, key: Long, acceptor: UUID, serialNumber: Int, proposer: UUID, epoch: Int) {
      events(ReceivedAccepted(instanceId, key, acceptor, serialNumber, proposer, epoch))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventSentDecide(instanceId: String, key: Long, op: Int, epoch: Int, value: Option[StatePaxosData]) {
      events(SentDecide(instanceId, key, op, epoch, value))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventSentRejectedEpoch(instanceId: String, key: Long, epoch: Int) {
      events(SentRejectedEpoch(instanceId, key, epoch))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventReceivedRejected(instanceId: String, key: Long, acceptor: UUID, serialNumber: Int, proposer: UUID, epoch: Int) {
      events(ReceivedRejectedVersion(instanceId, key, serialNumber, proposer, epoch))
  }
}