package org.opensplice.mobile.dev.dadds

import org.omg.dds.core.ServiceEnvironment


object Status {
  import org.omg.dds.core.ServiceEnvironment

  def AllStatus(implicit env: ServiceEnvironment) = env.getSPI.allStatusKinds()

  def NoStatus(implicit env: ServiceEnvironment) = env.getSPI.noStatusKinds()
}
