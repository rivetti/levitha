package org.opensplice.mobile.dev.dadds
import org.omg.dds.core.event.DataAvailableEvent
import org.omg.dds.core.event.LivelinessChangedEvent
import org.omg.dds.core.event.RequestedDeadlineMissedEvent
import org.omg.dds.core.event.RequestedIncompatibleQosEvent
import org.omg.dds.core.event.SampleLostEvent
import org.omg.dds.core.event.SampleRejectedEvent
import org.omg.dds.core.event.SubscriptionMatchedEvent

object prelude {
  import org.omg.dds.core.event._
  import org.omg.dds.sub.DataReader

  implicit class ReaderListener[T]( val fun: PartialFunction[Any, Unit] )
      extends org.omg.dds.sub.DataReaderListener[T] {

    def onRequestedDeadlineMissed( e: RequestedDeadlineMissedEvent[T] ) {}

    def onRequestedIncompatibleQos( e: RequestedIncompatibleQosEvent[T] ) {}

    def onSampleRejected( e: SampleRejectedEvent[T] ) {}

    def onLivelinessChanged( e: LivelinessChangedEvent[T] ) {}

    def onDataAvailable( e: DataAvailableEvent[T] ) {
      val evt = DataAvailable( e.getSource.asInstanceOf[DataReader[T]] )
      if ( fun.isDefinedAt( evt ) ) fun( evt )
    }

    def onSubscriptionMatched( e: SubscriptionMatchedEvent[T] ) {
      //println("onSubscriptionMatched: " + e.getSource().toString())
      val evt = SubscriptionMatched( e.getSource.asInstanceOf[DataReader[T]] )
      if ( fun.isDefinedAt( evt ) ) fun( evt )
    }

    def onSampleLost( e: SampleLostEvent[T] ) {}
  }
}
