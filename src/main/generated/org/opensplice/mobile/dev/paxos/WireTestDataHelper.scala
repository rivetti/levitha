package org.opensplice.mobile.dev.paxos

import nuvo.nio.prelude._
import nuvo.nio._
import nuvo.core.Tuple

object WireTestDataTypeRegistration {
  val typeList = List("org.opensplice.mobile.dev.paxos.WireTestData")
  
  var registerTypeOK = {typeList.foreach(nuvo.nio.SerializerCache.registerType(_)); true}
}

object WireTestDataHelper {

  val typeHash = (-7011594239971702528L, -3378274860490740145L) // org.opensplice.mobile.dev.paxos.WireTestData
  def serialize(buf: RawBuffer, t: WireTestData, format: SerializationFormat) {
    format match {
    case NuvoSF  => serializeNuvoSF(buf, t)
    }
  }
   
  def serializeKey(buf: RawBuffer, t: WireTestData, format: SerializationFormat) {
    format match {
    case NuvoSF  => serializeNuvoSF(buf, t)
    }
  }
   
  final def serializeNuvoSF(buf: RawBuffer, t: WireTestData) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putString(t.data)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }
  
  final def serializeKeyNuvoSF(buf: RawBuffer, t: WireTestData) = ()
  
  def deserialize(buf: RawBuffer,format: SerializationFormat):WireTestData = {
    format match {
    case NuvoSF  => deserializeNuvoSF(buf)
    }
  }
  
  def deserializeKey(buf: RawBuffer,format: SerializationFormat) = {
    format match {
    case NuvoSF  => deserializeNuvoSF(buf)
    }
  }
  
  final def deserializeNuvoSF(buf: RawBuffer) : WireTestData = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess =  (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength =  (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value  => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength) ; throw new RuntimeException("Invalid Format")}})
    val __nuvoc_startPosition =  buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val data = buf.getString()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new WireTestData(data )
  }
  
  def deserializeNoHeaderNuvoSF(buf: RawBuffer) : WireTestData = {
    val data = buf.getString()
    new WireTestData(data )
  }
  
  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

