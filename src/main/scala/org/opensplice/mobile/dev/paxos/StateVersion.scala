package org.opensplice.mobile.dev.paxos

import java.util.UUID

object NullStateVersion extends StateVersion( -1, new UUID( -1, -1 ), -1 )

case class StateVersion( serialNumber: Int, proposer: UUID, epoch: Int = 0 ) extends Ordered[StateVersion] {
  def this( serialNumber: Int, epoch: Int = 0 )( implicit proposer: UUID ) = this( serialNumber, proposer, epoch )

  def compare( that: StateVersion ) = {
    val snDiff = this.serialNumber - that.serialNumber
    if ( snDiff != 0 ) {
      snDiff
    } else {
      val idDiff = this.proposer.compareTo( that.proposer )
      if ( idDiff != 0 ) {
        idDiff
      } else {
        this.epoch - that.epoch
      }
    }
  }
}

