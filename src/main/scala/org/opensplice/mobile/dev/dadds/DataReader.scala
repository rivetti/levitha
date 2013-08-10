package org.opensplice.mobile.dev.dadds

object DataReader {

  import org.opensplice.mobile.dev.dadds.{ Subscriber => DASubscriber }
  import org.opensplice.mobile.dev.dadds.{ Topic => DATopic }

  import org.omg.dds.sub.{ Subscriber => DDSSubscriber }
  import org.omg.dds.topic.{ Topic => DDSTopic }
  import org.omg.dds.sub.{ DataReader => DDSDataReader, DataReaderQos => DDSDataReaderQos }
  import org.omg.dds.core.policy.{ QosPolicy => DDSQosPolicy, PolicyFactory => DDSPolicyFactory }
  import org.omg.dds.domain.{ DomainParticipant => DDSDomainParticipant }

  def apply[T](sub: DDSSubscriber, topicClass: Class[T], policy: DDSQosPolicy.ForDataReader*)(implicit m: Manifest[T], dp: DDSDomainParticipant) =
    sub.createDataReader(DATopic[T]()(m, dp), sub.getDefaultDataReaderQos().withPolicies(policy: _*))

  def apply[T](partitions: List[String], topic: DDSTopic[T], policy: DDSQosPolicy.ForDataReader*)(implicit dp: DDSDomainParticipant, policyFactory: DDSPolicyFactory) = {
    val sub = DASubscriber(partitions: _*)(dp, policyFactory)
    sub.createDataReader(topic, sub.getDefaultDataReaderQos().withPolicies(policy: _*))
  }

  def apply[T](partitions: List[String], topicClass: Class[T], policy: DDSQosPolicy.ForDataReader*)(implicit m: Manifest[T], dp: DDSDomainParticipant, policyFactory: DDSPolicyFactory) = {
    val sub = DASubscriber(partitions: _*)(dp, policyFactory)
    sub.createDataReader(DATopic[T]()(m, dp), sub.getDefaultDataReaderQos().withPolicies(policy: _*))
  }

  // From Moliere
  def apply[T](sub: org.omg.dds.sub.Subscriber, t: DDSTopic[T]) = sub.createDataReader(t).asInstanceOf[org.omg.dds.sub.DataReader[T]]

  def apply[T](sub: org.omg.dds.sub.Subscriber, t: DDSTopic[T], qos: DDSDataReaderQos) =
    sub.createDataReader(t, qos).asInstanceOf[org.omg.dds.sub.DataReader[T]]

  def apply[T](t: DDSTopic[T])(implicit sub: org.omg.dds.sub.Subscriber) = sub.createDataReader(t).asInstanceOf[org.omg.dds.sub.DataReader[T]]

  def apply[T](t: DDSTopic[T], qos: DDSDataReaderQos)(implicit sub: org.omg.dds.sub.Subscriber) =
    sub.createDataReader(t, qos).asInstanceOf[org.omg.dds.sub.DataReader[T]]
}

