package org.opensplice.mobile.dev.common

import java.util.UUID

import scala.annotation.elidable
import scala.swing.Reactions

import org.omg.dds.core.ServiceEnvironment
import org.omg.dds.domain.DomainParticipant
import org.opensplice.mobile.dev.tools.DALogger

object DDSDAbstraction {
  var ESPER = false;
}

/**
 * Top Abstract class for the library abstractions, factoring out some facilities.
 */
abstract class DDSDAbstraction(val name: String, val identifier: DDSIdentifier) extends DALogger {

  val ESPER = DDSDAbstraction.ESPER

  val instanceId = identifier.instanceId
  val actorId = identifier.actorId

  implicit lazy val events = new Reactions.Impl

  logEvents()

  @elidable(ELIDABLE_EVENTS)
  def logEvents() {
    events += {
      case e: Any => {
        logger.info("#### EVENT #### {}", e.toString)
      }
    }
  }

  lazy val actorSystem = identifier.actorSystem

  implicit lazy val env: ServiceEnvironment = identifier.env

  implicit lazy val domainParticipant: DomainParticipant = identifier.domainParticipant

  implicit lazy val policyFactory = identifier.policyFactory

  def toShortString(uuid: UUID): String = org.opensplice.mobile.dev.common.toShortString(uuid)

  def close()

}