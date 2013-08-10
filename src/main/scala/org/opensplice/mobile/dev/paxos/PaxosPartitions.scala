package org.opensplice.mobile.dev.paxos

import org.opensplice.mobile.dev.common.DDSIdentifier

object PaxosPartitions {
  val SEPARATOR = ":"
  val ClientProposerPartitionPrefix = "%sPAXOS%sCLIENTPROPOSER".format(SEPARATOR, SEPARATOR)
  val ProposerExecutorPartitionPrefix = "%sPAXOS%sPROPOSEREXECUTOR".format(SEPARATOR, SEPARATOR)
  val ProposerAcceptorPartitionPrefix = "%sPAXOS%sPROPOSERACCEPTOR".format(SEPARATOR, SEPARATOR)

  def getClientProposerPartition(identifier: DDSIdentifier): String = {
    identifier.getParentInstanceId + ClientProposerPartitionPrefix
  }

  def getProposerExecutorPartition(identifier: DDSIdentifier): String = {
    identifier.getParentInstanceId + ProposerExecutorPartitionPrefix
  }

  def getProposerAcceptorPartition(identifier: DDSIdentifier): String = {
    identifier.getParentInstanceId + ProposerAcceptorPartitionPrefix
  }

}