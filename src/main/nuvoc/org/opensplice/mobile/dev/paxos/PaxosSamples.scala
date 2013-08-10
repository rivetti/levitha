package org.opensplice.mobile.dev.paxos

import nuvo.core.Tuple
import java.io.PrintWriter
import org.opensplice.mobile.dev.common.Uuid

sealed abstract class PaxosSamples extends Tuple

case class Propose(client: Uuid, statekey: Long, op: Int, epoch: Int, value: WirePaxosData) extends PaxosSamples {
  lazy val key = ()
}

case class Adopt(statekey: Long, serialNumber: Int, proposer: Uuid, epoch: Int) extends PaxosSamples {
  lazy val key = ()
}

case class Adopted(statekey: Long, acceptor: Uuid, serialNumber: Int, proposer: Uuid, epoch: Int, value: WirePaxosData,
  previousSerialNumber: Int, previousProposer: Uuid) extends PaxosSamples {
  lazy val key = ()
}

case class Accept(statekey: Long, serialNumber: Int, proposer: Uuid, epoch: Int, value: WirePaxosData) extends PaxosSamples {
  lazy val key = ()
}

case class Accepted(statekey: Long, acceptor: Uuid, serialNumber: Int, proposer: Uuid, epoch: Int) extends PaxosSamples {
  lazy val key = ()
}

case class Decide(statekey: Long, op: Int, epoch: Int, value: WirePaxosData) extends PaxosSamples {
  lazy val key = ()
}

case class RejectedVersion(statekey: Long, acceptor: Uuid, serialNumber: Int, proposer: Uuid, epoch: Int) extends PaxosSamples {
  lazy val key = ()
}

case class RejectedEpoch(statekey: Long, epoch: Int) extends PaxosSamples {
  lazy val key = ()
}