package org.opensplice.mobile.dev.dadds

import org.omg.dds.sub.{ DataReader => DDSDataReader }
import org.omg.dds.sub.DataReader.{ Selector => DDSSelector }
import org.omg.dds.sub.InstanceState
import org.omg.dds.sub.SampleState
import org.omg.dds.sub.ViewState

object Selector {

  import org.omg.dds.sub.{ DataReader => DDSDataReader }

  import org.omg.dds.sub.SampleState
  import org.omg.dds.sub.ViewState
  import org.omg.dds.sub.InstanceState

  import org.omg.dds.sub.DataReader.{ Selector => DDSSelector }

  def apply[T]( instanceStates: List[InstanceState],
                viewStates: List[ViewState],
                sampleStates: List[SampleState],
                reader: DDSDataReader[T] ): DDSSelector[T] = {
    val selector = reader.select()
    val dataState = reader.getParent().createDataState()

    if ( instanceStates == null || instanceStates.isEmpty ) {
      dataState.withAnyInstanceState()
    } else {
      instanceStates.foreach( state => dataState.`with`( state ) )
    }

    if ( viewStates == null || viewStates.isEmpty ) {
      dataState.withAnyViewState()
    } else {
      viewStates.foreach( state => dataState.`with`( state ) )
    }

    if ( sampleStates == null || sampleStates.isEmpty ) {
      dataState.withAnySampleState()
    } else {
      sampleStates.foreach( state => dataState.`with`( state ) )
    }
    selector.dataState( dataState )
  }
}
