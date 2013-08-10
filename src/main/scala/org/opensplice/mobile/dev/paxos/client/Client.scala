package org.opensplice.mobile.dev.paxos.client

import java.util.UUID

import scala.annotation.elidable

import org.opensplice.mobile.dev.common.{DDSDAbstraction, DDSIdentifier, ELIDABLE_EVENTS}
import org.opensplice.mobile.dev.paxos.StatePaxosData
import org.opensplice.mobile.dev.paxos.event.ReceivedRejectedEpoch

abstract class Client(name: String, group: DDSIdentifier) extends DDSDAbstraction(name, group) {

  @elidable(ELIDABLE_EVENTS)
  def eventReceivedRejectedEpoch(instanceId: String, key: Long, epoch: Int) {
      events(new ReceivedRejectedEpoch(instanceId, key, epoch))
  }

  def write(cid: UUID, key: Long, epoch: Int, value: StatePaxosData)
  def write(cid: UUID, key: Long, value: StatePaxosData)

  def write_owner(cid: UUID, key: Long, epoch: Int, value: StatePaxosData)
  def write_owner(cid: UUID, key: Long, value: StatePaxosData)

  def take(cid: UUID, key: Long, epoch: Int)
  def take(cid: UUID, key: Long)

  def read(cid: UUID, key: Long)

  def read_last(cid: UUID, key: Long)

  def add(cid: UUID, key: Long, epoch: Int, value: StatePaxosData)
  def add(cid: UUID, key: Long, value: StatePaxosData)

  def add_owner(cid: UUID, key: Long, epoch: Int, value: StatePaxosData)
  def add_owner(cid: UUID, key: Long, value: StatePaxosData)

  def remove(cid: UUID, key: Long, epoch: Int, value: StatePaxosData)
  def remove(cid: UUID, key: Long, value: StatePaxosData)

}