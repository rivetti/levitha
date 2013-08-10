package org.opensplice.mobile.dev.paxos

object PaxosExceptions {
  
  case class AddOperationNotSupported() extends Exception
  case class AddOwnerOperationNotSupported() extends Exception
  case class RemoveOperationNotSupported() extends Exception
  case class RemoveOwnerOperationNotSupported() extends Exception

}