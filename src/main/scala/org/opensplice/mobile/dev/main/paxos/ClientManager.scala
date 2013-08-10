package org.opensplice.mobile.dev.main.paxos

import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.main.Manager
import org.opensplice.mobile.dev.paxos.client.ClientImpl
import org.opensplice.mobile.dev.paxos.event.ReceivedRejectedEpoch
import org.opensplice.mobile.dev.tools.DALogger
import org.opensplice.mobile.dev.paxos.TestData

object ClientManager {
  val SENDPROPOSAL = "proposal"
}

class ClientManager(identifier: DDSIdentifier) extends Manager with DALogger {

  import ClientManager._

  var client: Option[ClientImpl] = None

  override def manageCommand(commands: List[String]) {

    commands match {
      case CLOSE :: Nil => {
        if (client.isDefined) {
          client.get.close()
        }
      }

      case "write" :: keyString :: epochString :: value :: Nil => {
        val epoch = epochString.toInt
        val key = keyString.toInt
        if (client.isDefined) {
          client.get.write(identifier.actorId, key, epoch, new TestData(value))
        }
      }

      case "read" :: keyString :: Nil => {
        val key = keyString.toInt
        if (client.isDefined) {
          client.get.read(identifier.actorId, key)
        }
      }

      case "take" :: keyString :: Nil => {
        val key = keyString.toInt
        if (client.isDefined) {
          client.get.take(identifier.actorId, key)
        }
      }

      case "add" :: keyString :: epochString :: value :: Nil => {
        val epoch = epochString.toInt
        val key = keyString.toInt
        if (client.isDefined) {
          client.get.add(identifier.actorId, key, epoch, new TestData(value))
        }
      }

      case "remove" :: keyString :: epochString :: value :: Nil => {
        val epoch = epochString.toInt
        val key = keyString.toInt
        if (client.isDefined) {
          client.get.remove(identifier.actorId, key, epoch, new TestData(value))
        }
      }

      case _ => {}
    }

  }

  override def manageArgs(args: List[String]) {
    args match {
      case Nil => {

        client = Some(ClientImpl(identifier, TestData.state2Wire))

        client.get.events += {
          case ReceivedRejectedEpoch(_, k, e) => {

            logger.info("[%d] Received rejected epoch %d".format(k, e))

          }

          case _ => logger.debug("?")
        }

      }
      case _ => {
        println("Paxos Client Usage:\n" +
          "none create standard client\n")
      }
    }
  }
}