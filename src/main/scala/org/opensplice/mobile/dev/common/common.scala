package org.opensplice.mobile.dev

import java.util.UUID
import scala.language.implicitConversions
import org.opensplice.mobile.dev.gen.uuid
import nuvo.nio.RawBuffer
package object common {

  final val ELIDABLE_TRACE = 3100
  final val ELIDABLE_DEBUG = 3200
  final val ELIDABLE_INFO = 3300
  final val ELIDABLE_WARN = 3400
  final val ELIDABLE_ERROR = 3500
  final val ELIDABLE_LOGGER = 3600
  final val ELIDABLE_EVENTS = 4000
  final val ELIDABLE_ESPER = 2000

  val STRING_LENGHT_UUID = 5

  // Implicit conversion between Topic level org.opensplice.mobile.dev.gen.uuid and Lib Level java.util.UUID
  implicit def uuid2UUID(id: uuid): UUID = new UUID(id.high, id.low)
  implicit def Uuid2UUID(id: Uuid): UUID = new UUID(id.high, id.low)
  implicit def UUID2uuid(id: UUID): uuid = new uuid(id.getMostSignificantBits, id.getLeastSignificantBits)
  implicit def UUID2Uuid(id: UUID): Uuid = Uuid(id.getMostSignificantBits, id.getLeastSignificantBits)

  def serializeUuid(buf: RawBuffer, t: Uuid) {
    buf.putLong(t.high)
    buf.putLong(t.low)
  }

  def deserializeUuid(buf: RawBuffer): Uuid = {
    val high = buf.getLong()
    val low = buf.getLong()
    new Uuid(high, low)
  }

  def serializeUUID(buf: RawBuffer, t: UUID) {
    buf.putLong(t.getMostSignificantBits())
    buf.putLong(t.getLeastSignificantBits())
  }

  def deserializeUUID(buf: RawBuffer): UUID = {
    val high = buf.getLong()
    val low = buf.getLong()
    new UUID(high, low)
  }

  // Facility to reduce the length of UUID printed string
  def toShortString(uuid: UUID): String = {
    val string = uuid.toString
    string.substring(string.length() - STRING_LENGHT_UUID, string.length())
  }

  val NULLEPOCH = -1
}