package org.opensplice.mobile.dev.dadds

import org.omg.dds.core.policy.PolicyFactory
import scala.collection.JavaConversions._

object Partition {
  def apply(names: String*)(implicit pf: PolicyFactory) = pf.Partition().withName(names)

  // From Moliere

  def apply(name: String)(implicit pf: PolicyFactory) = pf.Partition().withName(name)
  def apply(names: List[String])(implicit pf: PolicyFactory) = pf.Partition().withName(names)
}

object History {
  def KeepLast()(implicit pf: PolicyFactory) = pf.History().withKeepLast(1)
  def KeepAll()(implicit pf: PolicyFactory) = pf.History().withKeepAll()
  // From Moliere

  def KeepLast(depth: Int)(implicit pf: PolicyFactory) = pf.History().withKeepLast(depth)
  def KeepAll(depth: Int)(implicit pf: PolicyFactory) = pf.History().withKeepAll()
}

object EntityFactory {
  def NotEnabled()(implicit pf: PolicyFactory) = pf.EntityFactory().withAutoEnableCreatedEntities(false)
}

object WriterDataLifecycle { // extends WriterDataLifecycle {
  def AutoDisposeUnregisteredInstances(bool: Boolean)(implicit pf: PolicyFactory) =
    pf.WriterDataLifecycle().withAutDisposeUnregisteredInstances(bool);
}

// From Moliere

object Reliability {
  def BestEffort()(implicit pf: PolicyFactory) = pf.Reliability().withBestEffort()
  //def Reliable()( implicit pf: PolicyFactory ) = pf.Reliability().withReliable()
  def Reliable()(implicit pf: PolicyFactory) = pf.Reliability().withBestEffort()
}

object Durability {
  def Volatile()(implicit pf: PolicyFactory) = pf.Durability().withVolatile()
  def TransientLocal()(implicit pf: PolicyFactory) = pf.Durability().withTransientLocal()
  def Transient()(implicit pf: PolicyFactory) = pf.Durability().withTransient()
  def Persistent()(implicit pf: PolicyFactory) = pf.Durability().withPersitent()
}

