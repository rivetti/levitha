package org.opensplice.mobile.dev.common

import java.util.UUID

import scala.util.Random

import akka.actor.ActorSystem

import org.omg.dds.core.ServiceEnvironment
import org.omg.dds.core.policy.PolicyFactory
import org.omg.dds.domain.DomainParticipant
import org.opensplice.mobile.dev.tools.DALogger

import DDSIdentifier.{DEFAULT_DOMAIN, ROOT_INSTANCE_ID}

object RootIdentifier extends DDSIdentifier("Root", new UUID(0, 0), DEFAULT_DOMAIN, None) {

  override def toString(): String = {
    "Root Identifier"
  }

}

object DDSIdentifier {
  val rand = new Random()
  def DEFAULT_MEMBERID = new UUID(0, rand.nextInt(100000))
  val DEFAULT_GROUP_ID = "DefaultGroup"
  val DEFAULT_DOMAIN = 0
  val DEFAULT_PARENT = RootIdentifier
  val ROOT_INSTANCE_ID = "Root"
  val SEPARATOR = ":"

  /*
   * With Parent
   */

  // With Group ID and Member ID

  def apply(groupId: String, memberId: UUID, domain: Int, parentGroup: DDSIdentifier): DDSIdentifier =
    new DDSIdentifier(parentGroup.instanceId + SEPARATOR + groupId, memberId, domain, Some(parentGroup))

  def apply(groupId: String, memberId: UUID, parentGroup: DDSIdentifier): DDSIdentifier =
    DDSIdentifier(groupId, memberId, parentGroup.domain, parentGroup)

  
    // With Group ID and without Member ID

  def apply(groupId: String, domain: Int, parentGroup: DDSIdentifier): DDSIdentifier =
    DDSIdentifier(groupId, parentGroup.actorId, domain, parentGroup)

  def apply(groupId: String, parentGroup: DDSIdentifier): DDSIdentifier =
    DDSIdentifier(groupId, parentGroup.actorId, parentGroup.domain, parentGroup)

  /*
   * Without Parent
   */

  // With Group ID and Member ID

  def apply(groupId: String, memberId: UUID, domain: Int): DDSIdentifier =
    DDSIdentifier(groupId, memberId, domain, RootIdentifier)

  def apply(groupId: String, memberId: UUID): DDSIdentifier =
    DDSIdentifier(groupId, memberId, DEFAULT_DOMAIN, RootIdentifier)

  // With Group ID and without Member ID

  def apply(groupId: String, domain: Int): DDSIdentifier =
    DDSIdentifier(groupId, DEFAULT_MEMBERID, domain, RootIdentifier)

  def apply(groupId: String): DDSIdentifier =
    DDSIdentifier(groupId, DEFAULT_MEMBERID, DEFAULT_DOMAIN, RootIdentifier)

  // With Member ID and without Group ID

  def apply(memberId: UUID, domain: Int): DDSIdentifier =
    DDSIdentifier(DEFAULT_GROUP_ID, memberId, domain, RootIdentifier)

  def apply(memberId: UUID): DDSIdentifier =
    DDSIdentifier(DEFAULT_GROUP_ID, memberId, DEFAULT_DOMAIN, RootIdentifier)

  // Without Group ID and Member ID

  def apply(domain: Int): DDSIdentifier =
    DDSIdentifier(DEFAULT_GROUP_ID, DEFAULT_MEMBERID, domain, RootIdentifier)

  def apply(): DDSIdentifier =
    DDSIdentifier(DEFAULT_GROUP_ID, DEFAULT_MEMBERID, DEFAULT_DOMAIN, RootIdentifier)

}

/**
 * Encapsulates the Instance ID and Actor ID, as well as the DDS relevant domain.
 * It builds up a hierarchy among DDSIdentifier in order to capture the instanciated abstraction hierarchy and avoid name space clashes
 * It also factors out some facilities
 */
class DDSIdentifier(instanceId: String, actorId: UUID, val domain: Int, parent: Option[DDSIdentifier])
    extends DAIdentifier(instanceId, actorId) with DALogger {

  /**
   * Return the parent InstanceId.
   * In example this can
   */
  def getParentInstanceId = {
    if (parent.isDefined) {
      parent.get.instanceId
    } else {
      ROOT_INSTANCE_ID
    }
  }

  lazy val actorSystem = ActorSystem("DDSDAbstraction")

  lazy val env: ServiceEnvironment = {
    if (parent.isDefined) {
      parent.get.env
    } else {
      System.setProperty(ServiceEnvironment.IMPLEMENTATION_CLASS_NAME_PROPERTY,
        "org.opensplice.mobile.core.ServiceEnvironmentImpl")
      ServiceEnvironment.createInstance(classOf[DDSIdentifier].getClassLoader())
    }
  }

  lazy val domainParticipant: DomainParticipant = {
    if (parent.isDefined) {
      parent.get.domainParticipant
    } else {
      val dp = env.getSPI().getParticipantFactory().createParticipant(domain)
      logger.info("Create DP")
      dp
    }
  }

  lazy val policyFactory = {
    PolicyFactory.getPolicyFactory(env)

    /*if ( parent.isDefined ) {
      parent.get.policyFactory
    } else {
      PolicyFactory.getPolicyFactory( env )
    }*/
  }

  override def toString(): String = {
    "Identifier %s with MemberID: %s in Domain: %d".format(instanceId, toShortString(actorId), domain)
  }

  logger.info("Created " + toString)
}
