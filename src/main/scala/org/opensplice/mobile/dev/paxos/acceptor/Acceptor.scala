package org.opensplice.mobile.dev.paxos.acceptor

import java.util.UUID

import scala.annotation.elidable

import org.opensplice.mobile.dev.common.{DDSDAbstraction, DDSIdentifier, ELIDABLE_EVENTS}
import org.opensplice.mobile.dev.paxos.StatePaxosData
import org.opensplice.mobile.dev.paxos.event.{ReceivedAccept, ReceivedAdopt, SentAccepted, SentAdopted, SentRejected}

abstract class Acceptor(name: String, group: DDSIdentifier) extends DDSDAbstraction(name, group) {

  @elidable(ELIDABLE_EVENTS)
  def eventReceivedAdopt(instanceId: String, key: Long, serialNumber: Int, proposer: UUID, epoch: Int) {
      events(ReceivedAdopt(instanceId, key, serialNumber, proposer, epoch))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventSentAdopted(instanceId: String, key: Long, serialNumber: Int, proposer: UUID, epoch: Int, value: Option[StatePaxosData], previousSn: Int, previousProposer: UUID) {
      events(SentAdopted(instanceId, key, serialNumber, proposer, epoch, value, previousSn, previousProposer))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventReceivedAccept(instanceId: String, key: Long, serialNumber: Int, proposer: UUID, epoch: Int, value: Option[StatePaxosData]) {
      events(ReceivedAccept(instanceId, key, serialNumber, proposer, epoch, value))
  }

  @elidable(ELIDABLE_EVENTS)
  def eventSentAccepted(instanceId: String, key: Long, serialNumber: Int, proposer: UUID, epoch: Int) {
      events(SentAccepted(instanceId, key, serialNumber, proposer, epoch))

  }

  @elidable(ELIDABLE_EVENTS)
  def eventSentRejected(instanceId: String, key: Long, serialNumber: Int, proposer: UUID, epoch: Int) {
      events(SentRejected(instanceId, key, serialNumber, proposer, epoch))
  }

}
