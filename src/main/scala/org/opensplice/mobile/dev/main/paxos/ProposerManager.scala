package org.opensplice.mobile.dev.main.paxos

import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.main.Manager
import org.opensplice.mobile.dev.paxos.proposer.ProposerImpl
import org.opensplice.mobile.dev.paxos.proposer.ProposerImpl.Default.leaderFactory
import org.opensplice.mobile.dev.tools.DALogger
import org.opensplice.mobile.dev.paxos.TestData
import org.opensplice.mobile.dev.paxos.proposer.ProposerImpl.Default._

class ProposerManager(identifier: DDSIdentifier) extends Manager with DALogger {
  import org.opensplice.mobile.dev.paxos.proposer.ProposerImpl.Default._

  var proposer: Option[ProposerImpl] = None

  override def manageCommand(commands: List[String]) {

    commands match {
      case CLOSE :: Nil => {
        if (proposer.isDefined)
          proposer.get.close()
      }

      case _ => {}
    }

  }

  override def manageArgs(args: List[String]) {
    args match {
      case Nil => {

        proposer = Some(ProposerImpl(identifier, TestData.state2Wire, TestData.wire2State))
      }

      case "-aq" :: value :: tail => {

        val valueInt = value.toInt

        println("Acceptor Quorum: %d".format(valueInt))

        proposer = Some(ProposerImpl(identifier, TestData.state2Wire, TestData.wire2State, valueInt))
      }

      case _ => {
        println("Paxos Proposer Usage:\n" +
          "none create standard proposer\n" +
          "-aq <value> create a proposer with value as acceptor quorum ")
      }

    }
  }

}