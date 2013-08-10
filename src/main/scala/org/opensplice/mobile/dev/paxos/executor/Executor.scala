package org.opensplice.mobile.dev.paxos.executor

import org.opensplice.mobile.dev.common.{DDSDAbstraction, DDSIdentifier}



abstract class Executor(name: String, identifier: DDSIdentifier) extends DDSDAbstraction(name, identifier) {

}