package org.opensplice.mobile.dev.dadds

object DataWriter  {

  import org.opensplice.mobile.dev.dadds.{ Publisher => DAPublisher }
  import org.opensplice.mobile.dev.dadds.{ Topic => DATopic }

  import org.omg.dds.pub.{ Publisher => DDSPublisher }
  import org.omg.dds.topic.{ Topic => DDSTopic }
  import org.omg.dds.pub.{ DataWriter => DDSDataWriter, DataWriterQos => DDSDataWriterQos }
  import org.omg.dds.core.policy.{ QosPolicy => DDSQosPolicy, PolicyFactory => DDSPolicyFactory }
  import org.omg.dds.domain.{ DomainParticipant => DDSDomainParticipant }

  import scala.collection.JavaConversions._

  def apply[T](pub: DDSPublisher, topicClass: Class[T], policy: DDSQosPolicy.ForDataWriter*)(implicit m: Manifest[T], dp: DDSDomainParticipant) =
    pub.createDataWriter(DATopic[T]()(m, dp), pub.getDefaultDataWriterQos().withPolicies(policy: _*))

  def apply[T](partitions: List[String], topic: DDSTopic[T], policy: DDSQosPolicy.ForDataWriter*)(implicit dp: DDSDomainParticipant, policyFactory: DDSPolicyFactory) = {
    val pub = DAPublisher(partitions: _*)(dp, policyFactory)
    pub.createDataWriter(topic, pub.getDefaultDataWriterQos().withPolicies(policy: _*))
  }

  def apply[T](partitions: List[String], topicClass: Class[T], policy: DDSQosPolicy.ForDataWriter*)(implicit m: Manifest[T], dp: DDSDomainParticipant, policyFactory: DDSPolicyFactory) = {
    val pub = DAPublisher(partitions: _*)(dp, policyFactory)
    pub.createDataWriter(DATopic[T]()(m, dp), pub.getDefaultDataWriterQos().withPolicies(policy: _*))
  }

  // From Moliere

  def create[T](pub: org.omg.dds.pub.Publisher, t: DDSTopic[T]) = {
    pub.createDataWriter[T](t)
  }

  def apply[T](pub: org.omg.dds.pub.Publisher, t: DDSTopic[T]) = {
    pub.createDataWriter[T](t)
  }

  def apply[T](pub: DDSPublisher, t: DDSTopic[T], qos: DDSDataWriterQos) = {
    pub.createDataWriter[T](t, qos, null, List())
  }

  def apply[T](t: DDSTopic[T])(implicit pub: org.omg.dds.pub.Publisher, m: Manifest[T]) = {
    pub.createDataWriter[T](t).asInstanceOf[org.omg.dds.pub.DataWriter[T]]

  }

  def apply[T](t: DDSTopic[T], qos: DDSDataWriterQos)(implicit pub: DDSPublisher) =
    pub.createDataWriter[T](t, qos, null, List())
}


