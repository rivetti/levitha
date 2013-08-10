package org.opensplice.mobile.dev.paxos

import nuvo.spaces.Space
import nuvo.spaces.SpaceLocator
import nuvo.core.Tuple
import scala.collection.mutable.HashMap
import nuvo.spaces.remote.SpaceServer
import nuvo.spaces.RemoteSpaceLocator
import java.io.IOException
import nuvo.spaces.Stream

object TupleSpace {

  val hosts = new HashMap[String, String]()

  hosts("askja") = "localhost" //"10.100.1.141"
  hosts("katla") = "10.100.1.142"
  hosts("nuku-hivo") = "10.100.1.183"
  hosts("maria.fr.topgraphx.com") = "10.100.1.223"

  val locStrings = new Array[String](4)
  locStrings(0) = "pcpaxos@spaces:ndr/tcp:%s:9999".format(hosts("askja"))
  locStrings(1) = "pcpaxos@spaces:ndr/tcp:%s:9999".format(hosts("katla"))
  locStrings(2) = "pcpaxos@spaces:ndr/tcp:%s:9999".format(hosts("nuku-hivo"))
  locStrings(3) = "pcpaxos@spaces:ndr/tcp:%s:9999".format(hosts("maria.fr.topgraphx.com"))

  val locStringsAcceptor = new Array[String](4)
  locStringsAcceptor(0) = "acceptor@spaces:ndr/tcp:%s:9998".format(hosts("askja"))
  locStringsAcceptor(1) = "acceptor@spaces:ndr/tcp:%s:9998".format(hosts("katla"))
  locStringsAcceptor(2) = "acceptor@spaces:ndr/tcp:%s:9998".format(hosts("nuku-hivo"))
  locStringsAcceptor(3) = "acceptor@spaces:ndr/tcp:%s:9998".format(hosts("maria.fr.topgraphx.com"))

  val locators = new Array[Option[SpaceLocator]](4)
  locators(0) = SpaceLocator(locStrings(0))
  locators(1) = SpaceLocator(locStrings(1))
  locators(2) = SpaceLocator(locStrings(2))
  locators(3) = SpaceLocator(locStrings(3))

  val locatorsAcceptor = new Array[Option[SpaceLocator]](4)
  locatorsAcceptor(0) = SpaceLocator(locStringsAcceptor(0))
  locatorsAcceptor(1) = SpaceLocator(locStringsAcceptor(1))
  locatorsAcceptor(2) = SpaceLocator(locStringsAcceptor(2))
  locatorsAcceptor(3) = SpaceLocator(locStringsAcceptor(3))

  private def getSpace(): Option[Space[PaxosSamples]] = {

    def attemptGetspace(attempts: Int): Option[Space[PaxosSamples]] = {
      if (attempts < 0) {
        None
      } else {
        try {
          println("creating space " + locStrings(attempts))
          val space = Space[PaxosSamples](locators(attempts))
          println("created space " + space.toString())
          space
        } catch {
          case ex: Throwable => {
            println("failure")
            attemptGetspace(attempts - 1)
          }
        }
      }

    }
    attemptGetspace(locators.size - 1)
  }

  private def getSpaceAcceptor(): Option[Space[PaxosSamples]] = {

    def attemptGetspace(attempts: Int): Option[Space[PaxosSamples]] = {
      if (attempts < 0) {
        None
      } else {
        try {
          println("creating Acceptor space " + locStringsAcceptor(attempts))
          val space = Space[PaxosSamples](locatorsAcceptor(attempts))
          println("created space " + space.toString())
          space
        } catch {
          case ex: Throwable => {
            println("failure")
            attemptGetspace(attempts - 1)
          }
        }
      }

    }
    attemptGetspace(locatorsAcceptor.size - 1)
  }

  /* lazy val spaceAcceptor = getSpaceAcceptor().getOrElse {
    throw new IOException("Cannot create space")
  }*/

  lazy val space = getSpace().getOrElse {
    throw new IOException("Cannot create space")
  }

  lazy val spaceAcceptor = space

  def createLocalSpaceServer() {
    val localhostname = java.net.InetAddress.getLocalHost().getHostName()

    println("Creating local space server pcpaxos@spaces:ndr/tcp:%s:9999".format(hosts(localhostname)))

    val loc = SpaceLocator("pcpaxos@spaces:ndr/tcp:%s:9999".format(hosts(localhostname)))
    loc.get match {
      case rmtloc: RemoteSpaceLocator => {
        new SpaceServer(rmtloc.locator).start
        println("server started at " + "pcpaxos@spaces:ndr/tcp:%s:9999".format(hosts(localhostname)))
      }
      case _ => {}
    }
  }

  def createLocalSpaceServerAcceptor() {
    /* val localhostname = java.net.InetAddress.getLocalHost().getHostName()

    println("Creating local space server pcpaxos@spaces:ndr/tcp:%s:9998".format(hosts(localhostname)))

    val loc = SpaceLocator("acceptor@spaces:ndr/tcp:%s:9998".format(hosts(localhostname)))
    loc.get match {
      case rmtloc: RemoteSpaceLocator => {
        new SpaceServer(rmtloc.locator).start
        println("server started")
      }
      case _ => {}
    }*/

  }

  var streams = List[Stream[_]]()

  // For Client (from Proposer)
  def addListenerRejectedEpoch(obs: RejectedEpoch => Unit) {
    val stream = space.stream((t: Tuple) => t match {
      case RejectedEpoch(_, _) => true
      case _ => false
    }, obs)
    streams = stream :: streams
  }

  // For Proposer (from Client)
  def addListenerPropose(obs: Propose => Unit) {
    val stream = space.stream((t: Tuple) => t match {
      case Propose(_, _, _, _, _) => true
      case _ => false
    }, obs)
    streams = stream :: streams
  }

  // For Proposer (from Acceptor)
  def addListenerAdopted(obs: Adopted => Unit) {
    val stream = spaceAcceptor.stream((t: Tuple) => t match {
      case e: Adopted => true
      case _ => false
    }, obs)
    streams = stream :: streams
  }

  // For Proposer (from Acceptor)
  def addListenerAccepted(obs: Accepted => Unit) {
    val stream = spaceAcceptor.stream((t: Tuple) => t match {
      case e: Accepted => true
      case _ => false
    }, obs)
    streams = stream :: streams
  }

  // For Proposer (from Acceptor)
  def addListenerRejectedVersion(obs: RejectedVersion => Unit) {
    val stream = spaceAcceptor.stream((t: Tuple) => t match {
      case e: RejectedVersion => true
      case _ => false
    }, obs)
    streams = stream :: streams
  }

  // For Acceptor (from Proposer)
  def addListenerAccept(obs: Accept => Unit) {
    val stream = spaceAcceptor.stream((t: Tuple) => t match {
      case e: Accept => true
      case _ => false
    }, obs)
    streams = stream :: streams
  }

  // For Acceptor (from Proposer)
  def addListenerAdopt(obs: Adopt => Unit) {
    val stream = spaceAcceptor.stream((t: Tuple) => t match {
      case e: Adopt => true
      case _ => false
    }, obs)
    streams = stream :: streams
  }

  // For Executor (from Proposer)
  def addListenerDecide(obs: Decide => Unit) {
    val stream = space.stream((t: Tuple) => t match {
      case Decide(_, _, _, _) => true
      case _ => false
    }, obs)
    streams = stream :: streams
  }

  // Generic
  def addListener(pred: Tuple => Boolean, obs: PaxosSamples => Unit) {
    val stream = space.stream(pred, obs)
    streams = stream :: streams
  }

  def removeAllListeners() {
    streams.foreach(stream => stream.close)
  }

  def closeSpace() {
    space.close()
  }

  // Generic
  def write(sample: Propose) {
    space.write(sample)
  }

  // Generic
  def write(sample: RejectedEpoch) {
    spaceAcceptor.write(sample)
  }

  // Generic
  def write(sample: Adopt) {
    spaceAcceptor.write(sample)
  }

  // Generic
  def write(sample: Adopted) {
    spaceAcceptor.write(sample)
  }

  // Generic
  def write(sample: Accept) {
    spaceAcceptor.write(sample)
  }

  // Generic
  def write(sample: Accepted) {
    spaceAcceptor.write(sample)
  }

  // Generic
  def write(sample: RejectedVersion) {
    spaceAcceptor.write(sample)
  }

  // Generic
  def write(sample: Decide) {
    space.write(sample)
  }
}