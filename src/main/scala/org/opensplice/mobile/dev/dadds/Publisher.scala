package org.opensplice.mobile.dev.dadds

object Publisher {
  import org.opensplice.mobile.dev.dadds.Partition
  import org.omg.dds.core.policy.PolicyFactory
  import org.omg.dds.domain.DomainParticipant
  import org.omg.dds.pub.PublisherQos

  def apply(partitions: String*)(implicit dp: DomainParticipant, policyFactory: PolicyFactory) = {
    dp.createPublisher(dp.getDefaultPublisherQos().withPolicy(Partition(partitions: _*)))
  }

  // From Moliere

  def apply(dp: DomainParticipant, qos: PublisherQos) = dp.createPublisher(qos)

  def apply(implicit dp: DomainParticipant) = dp.createPublisher()

  def apply(qos: PublisherQos)(implicit dp: DomainParticipant) = dp.createPublisher(qos)

}

