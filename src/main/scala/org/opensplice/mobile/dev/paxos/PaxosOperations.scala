package org.opensplice.mobile.dev.paxos

object PaxosOperations {

  val WRITE_OPS_LOW_LIMIT = 1
  val WRITE_OPS_HIGH_LIMIT = 128

  val WRITE = 1
  val WRITE_OWNER = 2
  val TAKE = 4

  val READ_OPS_LOW_LIMIT = 256
  val READ_OPS_HIGH_LIMIT = 32768

  val READ = 256;
  val READ_LAST = 512

  val INNER_OPS_LOW_LIMIT = 65536
  val INNER_OPS_HIGH_LIMIT = 8388608

  val ADD = 65536
  val ADD_OWNER = 131072
  val REMOVE = 262144

}