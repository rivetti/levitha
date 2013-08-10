package org.opensplice.mobile.dev.dadds

object Subscriber {
  import org.opensplice.mobile.dev.dadds.Partition
  import org.omg.dds.core.policy.PolicyFactory
  import org.omg.dds.domain.DomainParticipant
  import org.omg.dds.sub.SubscriberQos

  def apply( partitions: String* )( implicit dp: DomainParticipant, policyFactory: PolicyFactory ) = {
    val policies = dp.getDefaultSubscriberQos().withPolicy( Partition( partitions: _* ) ).withPolicy( EntityFactory.NotEnabled() )
    dp.createSubscriber( policies )
  }
  
  // From Moliere
  
  def apply(dp: DomainParticipant, qos: SubscriberQos) = dp.createSubscriber(qos)


  def apply(implicit dp: DomainParticipant) = dp.createSubscriber()

  def apply(qos: SubscriberQos)(implicit dp: DomainParticipant) = dp.createSubscriber(qos)

}





