package org.opensplice.mobile.dev.common

import java.util.UUID

/**
 * Top Abstract class for the object encapsulating data needed to distinguish among multiple instances, abstractions and actors
 */
abstract class DAIdentifier( val instanceId: String, val actorId: UUID ) {

}