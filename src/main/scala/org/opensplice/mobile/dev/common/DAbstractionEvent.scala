package org.opensplice.mobile.dev.common

import scala.swing.event.Event
import java.util.UUID

/**
 * Top Abstract class for Events raised by the library actors
 */
abstract class DAbstractionEvent extends Event {
  def toShortString( uuid: UUID ): String = org.opensplice.mobile.dev.common.toShortString( uuid )
}