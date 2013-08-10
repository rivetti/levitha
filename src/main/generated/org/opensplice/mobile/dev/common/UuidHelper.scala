package org.opensplice.mobile.dev.common

import nuvo.nio.prelude._
import nuvo.nio._
import nuvo.core.Tuple

object UuidTypeRegistration {
  val typeList = List("org.opensplice.mobile.dev.common.Uuid")
  
  var registerTypeOK = {typeList.foreach(nuvo.nio.SerializerCache.registerType(_)); true}
}

object UuidHelper {

  val typeHash = (-149585325384482611L, 6562886036406044647L) // org.opensplice.mobile.dev.common.Uuid
  def serialize(buf: RawBuffer, t: Uuid, format: SerializationFormat) {
    format match {
    case NuvoSF  => serializeNuvoSF(buf, t)
    }
  }
   
  final def serializeNuvoSF(buf: RawBuffer, t: Uuid) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putLong(t.high)
    buf.putLong(t.low)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }
  
  def deserialize(buf: RawBuffer,format: SerializationFormat):Uuid = {
    format match {
    case NuvoSF  => deserializeNuvoSF(buf)
    }
  }
  
  final def deserializeNuvoSF(buf: RawBuffer) : Uuid = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess =  (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength =  (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value  => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength) ; throw new RuntimeException("Invalid Format")}})
    val __nuvoc_startPosition =  buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val high = buf.getLong()
    val low = buf.getLong()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new Uuid(high , low)
  }
  
  def deserializeNoHeaderNuvoSF(buf: RawBuffer) : Uuid = {
    val high = buf.getLong()
    val low = buf.getLong()
    new Uuid(high , low)
  }
  
}

