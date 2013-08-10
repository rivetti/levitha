package org.opensplice.mobile.dev.group.stable

import nuvo.nio.prelude._
import nuvo.nio._
import nuvo.core.Tuple
import java.util.UUID

import scala.collection.immutable.SortedSet

object WireGroupStateTypeRegistration {
  val typeList = List("org.opensplice.mobile.dev.group.stable.WireGroupState")

  var registerTypeOK = { typeList.foreach(nuvo.nio.SerializerCache.registerType(_)); true }
}

object WireGroupStateHelper {

  val typeHash = (6788327922889009229L, 2644707008879242104L) // org.opensplice.mobile.dev.group.stable.WireGroupState
  def serialize(buf: RawBuffer, t: WireGroupState, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: WireGroupState, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: WireGroupState) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putInt(t.members.size)
    t.members.foreach { x =>
      org.opensplice.mobile.dev.common.serializeUUID(buf, x)
    }
    //t.members.foreach { x => buf.putObject(x) }
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: WireGroupState) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): WireGroupState = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): WireGroupState = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val __nuvoc_membersLen = buf.getInt()
    var members = SortedSet[UUID]()
    (1 to __nuvoc_membersLen foreach {
      x => members = members + org.opensplice.mobile.dev.common.deserializeUUID(buf)
    })
    //val members = (1 to __nuvoc_membersLen map { x => buf.getObject[Uuid]() }).toList
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new WireGroupState(members)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): WireGroupState = {
    val __nuvoc_membersLen = buf.getInt()
    var members = SortedSet[UUID]()
    (1 to __nuvoc_membersLen foreach {
      x => members = members + org.opensplice.mobile.dev.common.deserializeUUID(buf)
    })
    //val members = (1 to __nuvoc_membersLen map { x => buf.getObject[Uuid]() }).toList
    new WireGroupState(members)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

