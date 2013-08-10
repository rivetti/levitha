package org.opensplice.mobile.dev.paxos

import java.util.UUID

import org.opensplice.mobile.dev.paxos.PaxosExceptions.{ AddOperationNotSupported, AddOwnerOperationNotSupported, RemoveOperationNotSupported, RemoveOwnerOperationNotSupported }
//import esper.common.EsperData

trait StatePaxosData {
  def add(value: StatePaxosData): StatePaxosData
  def remove(value: StatePaxosData): StatePaxosData
  def update(add: List[(Int, StatePaxosData)], rmv: List[(Int, StatePaxosData)]): StatePaxosData
}

object NullStatePaxosData extends StatePaxosData {
  override def add(value: StatePaxosData) = { throw AddOperationNotSupported() }
  override def remove(value: StatePaxosData) = { throw AddOperationNotSupported() }
  override def update(add: List[(Int, StatePaxosData)], rmv: List[(Int, StatePaxosData)]) = { throw AddOperationNotSupported() }
}

trait WireData

trait CPWireData extends WireData
object NullCPWireData extends CPWireData

trait PCWireData extends WireData
object NullPCWireData extends PCWireData

trait PAWireData extends WireData
object NullPAWireData extends PAWireData

trait APWireData extends WireData
object NullAPWireData extends APWireData




trait WirePaxosData extends WireData

object NullWirePaxosData extends WirePaxosData

import nuvo.nio.prelude._
import nuvo.nio._
import nuvo.core.Tuple

object NullWirePaxosDataTypeRegistration {
  val typeList = List("org.opensplice.mobile.dev.paxos.NullWirePaxosData")

  var registerTypeOK = { typeList.foreach(nuvo.nio.SerializerCache.registerType(_)); true }
}

object NullWirePaxosData$Helper {

  val typeHash = (1234L, 1234L) // org.opensplice.mobile.dev.paxos.NullWirePaxosData
  def serialize(buf: RawBuffer, t: NullWirePaxosData.type, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: NullWirePaxosData.type, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: NullWirePaxosData.type) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: NullWirePaxosData.type) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): NullWirePaxosData.type = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): NullWirePaxosData.type = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    NullWirePaxosData
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): NullWirePaxosData.type = {
    NullWirePaxosData
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}
