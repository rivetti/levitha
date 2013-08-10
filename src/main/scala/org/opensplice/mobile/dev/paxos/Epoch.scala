package org.opensplice.mobile.dev.paxos

import org.opensplice.mobile.dev.paxos.Epoch.AnyEpoch
import org.opensplice.mobile.dev.paxos.Epoch.NullEpoch
import scala.language.implicitConversions 
object Epoch {

  implicit def nullEpoch2Long(value: NullEpoch.type): Int = NullEpoch()
  object NullEpoch extends Epoch {
    def apply() = Int.MinValue

  }

  implicit def anyEpoch2Long(value: AnyEpoch.type): Int = AnyEpoch()
  object AnyEpoch extends Epoch {
    def apply() = -1024

  }
}

class Epoch {

}