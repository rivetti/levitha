package org.opensplice.mobile.dev.paxos

import nuvo.nio.prelude._
import nuvo.nio._
import nuvo.core.Tuple

import org.opensplice.mobile.dev.common.Uuid

object PaxosSamplesTypeRegistration {
  val typeList = List("org.opensplice.mobile.dev.paxos.Propose", "org.opensplice.mobile.dev.paxos.Adopt", "org.opensplice.mobile.dev.paxos.Adopted", "org.opensplice.mobile.dev.paxos.Accept", "org.opensplice.mobile.dev.paxos.Accepted", "org.opensplice.mobile.dev.paxos.Decide", "org.opensplice.mobile.dev.paxos.RejectedVersion", "org.opensplice.mobile.dev.paxos.RejectedEpoch")

  var registerTypeOK = { typeList.foreach(nuvo.nio.SerializerCache.registerType(_)); true }
}

object ProposeHelper {

  val typeHash = (-5913457992674831318L, 827129285365610529L) // org.opensplice.mobile.dev.paxos.Propose
  def serialize(buf: RawBuffer, t: Propose, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: Propose, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: Propose) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    org.opensplice.mobile.dev.common.serializeUuid(buf, t.client)
    //buf.putObject(t.client)
    buf.putLong(t.statekey)
    buf.putInt(t.op)
    buf.putInt(t.epoch)
    buf.putObject(t.value)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: Propose) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): Propose = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): Propose = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val client = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val client = buf.getObject[Uuid]()
    val statekey = buf.getLong()
    val op = buf.getInt()
    val epoch = buf.getInt()
    val value = buf.getObject[WirePaxosData]()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new Propose(client, statekey, op, epoch, value)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): Propose = {
    val client = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val client = buf.getObject[Uuid]()
    val statekey = buf.getLong()
    val op = buf.getInt()
    val epoch = buf.getInt()
    val value = buf.getObject[WirePaxosData]()
    new Propose(client, statekey, op, epoch, value)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

object AdoptHelper {

  val typeHash = (4064821272009572339L, 1309856541390262730L) // org.opensplice.mobile.dev.paxos.Adopt
  def serialize(buf: RawBuffer, t: Adopt, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: Adopt, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: Adopt) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putLong(t.statekey)
    buf.putInt(t.serialNumber)
    org.opensplice.mobile.dev.common.serializeUuid(buf, t.proposer)
    //buf.putObject(t.proposer)
    buf.putInt(t.epoch)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: Adopt) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): Adopt = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): Adopt = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val statekey = buf.getLong()
    val serialNumber = buf.getInt()
    val proposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new Adopt(statekey, serialNumber, proposer, epoch)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): Adopt = {
    val statekey = buf.getLong()
    val serialNumber = buf.getInt()
    val proposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    new Adopt(statekey, serialNumber, proposer, epoch)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

object AdoptedHelper {

  val typeHash = (331608093171274555L, -3986708890607512155L) // org.opensplice.mobile.dev.paxos.Adopted
  def serialize(buf: RawBuffer, t: Adopted, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: Adopted, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: Adopted) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putLong(t.statekey)
    org.opensplice.mobile.dev.common.serializeUuid(buf, t.acceptor)
    //buf.putObject(t.acceptor)
    buf.putInt(t.serialNumber)
    org.opensplice.mobile.dev.common.serializeUuid(buf, t.proposer)
    //buf.putObject(t.proposer)
    buf.putInt(t.epoch)
    buf.putObject(t.value)
    buf.putInt(t.previousSerialNumber)
    org.opensplice.mobile.dev.common.serializeUuid(buf, t.proposer)
    //buf.putObject(t.previousProposer)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: Adopted) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): Adopted = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): Adopted = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val statekey = buf.getLong()
    val acceptor = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //acceptor = buf.getObject[Uuid]()
    val serialNumber = buf.getInt()
    val proposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    val value = buf.getObject[WirePaxosData]()
    val previousSerialNumber = buf.getInt()
    val previousProposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val previousProposer = buf.getObject[Uuid]()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new Adopted(statekey, acceptor, serialNumber, proposer, epoch, value, previousSerialNumber, previousProposer)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): Adopted = {
    val statekey = buf.getLong()
    val acceptor = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val acceptor = buf.getObject[Uuid]()
    val serialNumber = buf.getInt()
    val proposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    val value = buf.getObject[WirePaxosData]()
    val previousSerialNumber = buf.getInt()
    val previousProposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val previousProposer = buf.getObject[Uuid]()
    new Adopted(statekey, acceptor, serialNumber, proposer, epoch, value, previousSerialNumber, previousProposer)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

object AcceptHelper {

  val typeHash = (-5312736960000419235L, 8905248054417495962L) // org.opensplice.mobile.dev.paxos.Accept
  def serialize(buf: RawBuffer, t: Accept, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: Accept, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: Accept) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putLong(t.statekey)
    buf.putInt(t.serialNumber)
    org.opensplice.mobile.dev.common.serializeUuid(buf, t.proposer)
    //buf.putObject(t.proposer)
    buf.putInt(t.epoch)
    buf.putObject(t.value)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: Accept) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): Accept = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): Accept = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val statekey = buf.getLong()
    val serialNumber = buf.getInt()
    val proposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    val value = buf.getObject[WirePaxosData]()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new Accept(statekey, serialNumber, proposer, epoch, value)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): Accept = {
    val statekey = buf.getLong()
    val serialNumber = buf.getInt()
    val proposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    val value = buf.getObject[WirePaxosData]()
    new Accept(statekey, serialNumber, proposer, epoch, value)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

object AcceptedHelper {

  val typeHash = (3952933611115212986L, 2100986163802431951L) // org.opensplice.mobile.dev.paxos.Accepted
  def serialize(buf: RawBuffer, t: Accepted, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: Accepted, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: Accepted) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putLong(t.statekey)
    org.opensplice.mobile.dev.common.serializeUuid(buf, t.acceptor)
    //buf.putObject(t.acceptor)
    buf.putInt(t.serialNumber)
    org.opensplice.mobile.dev.common.serializeUuid(buf, t.proposer)
    //buf.putObject(t.proposer)
    buf.putInt(t.epoch)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: Accepted) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): Accepted = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): Accepted = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val statekey = buf.getLong()
    val acceptor = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val acceptor = buf.getObject[Uuid]()
    val serialNumber = buf.getInt()
    val proposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new Accepted(statekey, acceptor, serialNumber, proposer, epoch)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): Accepted = {
    val statekey = buf.getLong()
    val acceptor = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val acceptor = buf.getObject[Uuid]()
    val serialNumber = buf.getInt()
    val proposer = org.opensplice.mobile.dev.common.deserializeUuid(buf)
    //val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    new Accepted(statekey, acceptor, serialNumber, proposer, epoch)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

object DecideHelper {

  val typeHash = (3836235823849512753L, -6207857136567549040L) // org.opensplice.mobile.dev.paxos.Decide
  def serialize(buf: RawBuffer, t: Decide, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: Decide, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: Decide) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putLong(t.statekey)
    buf.putInt(t.op)
    buf.putInt(t.epoch)
    buf.putObject(t.value)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: Decide) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): Decide = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): Decide = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val statekey = buf.getLong()
    val op = buf.getInt()
    val epoch = buf.getInt()
    val value = buf.getObject[WirePaxosData]()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new Decide(statekey, op, epoch, value)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): Decide = {
    val statekey = buf.getLong()
    val op = buf.getInt()
    val epoch = buf.getInt()
    val value = buf.getObject[WirePaxosData]()
    new Decide(statekey, op, epoch, value)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

object RejectedVersionHelper {

  val typeHash = (-8206506404484259573L, -2226406451472359675L) // org.opensplice.mobile.dev.paxos.RejectedVersion
  def serialize(buf: RawBuffer, t: RejectedVersion, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: RejectedVersion, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: RejectedVersion) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putLong(t.statekey)
    buf.putObject(t.acceptor)
    buf.putInt(t.serialNumber)
    buf.putObject(t.proposer)
    buf.putInt(t.epoch)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: RejectedVersion) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): RejectedVersion = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): RejectedVersion = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val statekey = buf.getLong()
    val acceptor = buf.getObject[Uuid]()
    val serialNumber = buf.getInt()
    val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new RejectedVersion(statekey, acceptor, serialNumber, proposer, epoch)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): RejectedVersion = {
    val statekey = buf.getLong()
    val acceptor = buf.getObject[Uuid]()
    val serialNumber = buf.getInt()
    val proposer = buf.getObject[Uuid]()
    val epoch = buf.getInt()
    new RejectedVersion(statekey, acceptor, serialNumber, proposer, epoch)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

object RejectedEpochHelper {

  val typeHash = (4654629398526763083L, -825127635025370105L) // org.opensplice.mobile.dev.paxos.RejectedEpoch
  def serialize(buf: RawBuffer, t: RejectedEpoch, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  def serializeKey(buf: RawBuffer, t: RejectedEpoch, format: SerializationFormat) {
    format match {
      case NuvoSF => serializeNuvoSF(buf, t)
    }
  }

  final def serializeNuvoSF(buf: RawBuffer, t: RejectedEpoch) {
    buf.order(ByteOrder.nativeOrder)
    val __nuvoc_startPosition = buf.position
    buf.position(__nuvoc_startPosition + 4)
    buf.putLong(typeHash._1)
    buf.putLong(typeHash._2)
    buf.putLong(t.statekey)
    buf.putInt(t.epoch)
    val __nuvoc_serializedDataLength = buf.position - __nuvoc_startPosition - 4
    val __nuvoc_MEL = (buf.order.value << 24) | (__nuvoc_serializedDataLength & 0x00ffffff)
    buf.order(ByteOrder.littleEndian)
    buf.putInt(__nuvoc_startPosition, __nuvoc_MEL)
  }

  final def serializeKeyNuvoSF(buf: RawBuffer, t: RejectedEpoch) = ()

  def deserialize(buf: RawBuffer, format: SerializationFormat): RejectedEpoch = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  def deserializeKey(buf: RawBuffer, format: SerializationFormat) = {
    format match {
      case NuvoSF => deserializeNuvoSF(buf)
    }
  }

  final def deserializeNuvoSF(buf: RawBuffer): RejectedEpoch = {
    buf.order(LittleEndian)
    val __nuvoc_MEL = buf.getInt()
    val __nuvoc_endianess = (__nuvoc_MEL >> 24).toByte
    val __nuvoc_serializeDataLength = (__nuvoc_MEL & 0x00ffffff)
    buf.order(__nuvoc_endianess match { case LittleEndian.value => LittleEndian; case BigEndian.value => BigEndian; case _ => { buf.position(buf.position + __nuvoc_serializeDataLength); throw new RuntimeException("Invalid Format") } })
    val __nuvoc_startPosition = buf.position
    val wireTypeHash = (buf.getLong, buf.getLong)
    if (typeHash != wireTypeHash) throw new RuntimeException("Mismatching TypeHash, you ma be trying to deserialize using the wrong helper")
    val statekey = buf.getLong()
    val epoch = buf.getInt()
    buf.position(__nuvoc_startPosition + __nuvoc_serializeDataLength)
    new RejectedEpoch(statekey, epoch)
  }

  def deserializeNoHeaderNuvoSF(buf: RawBuffer): RejectedEpoch = {
    val statekey = buf.getLong()
    val epoch = buf.getInt()
    new RejectedEpoch(statekey, epoch)
  }

  final def deserializeKeyNuvoSF(buf: RawBuffer) = ()
  final def deserializeKeyNoHeaderNuvoSF(buf: RawBuffer) = ()
}

