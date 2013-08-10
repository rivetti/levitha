package org.opensplice.mobile.dev.paxos

case class WireTestData(data: String) extends WirePaxosData {
  lazy val key = ()
}