package org.opensplice.mobile.dev.main

import org.opensplice.mobile.dev.tools.DALogger

abstract class Manager extends DALogger {
  val CLOSE = "close"

  def manageArgs(args: List[String])
  def manageCommand(commands: List[String])

}