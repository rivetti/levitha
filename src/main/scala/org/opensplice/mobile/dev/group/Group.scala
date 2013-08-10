package org.opensplice.mobile.dev.group

import java.util.UUID
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import org.opensplice.mobile.dev.common.DDSDAbstraction
import org.opensplice.mobile.dev.common.DAIdentifier
import scala.collection.mutable.HashSet
import org.opensplice.mobile.dev.common.DDSIdentifier
import org.opensplice.mobile.dev.group.stable.TestHashSet

object Group {
  val POLL_INTERVAL = 1000
  val TIME_UNIT = TimeUnit.MILLISECONDS
}

abstract class Group( name: String, itentifier: DDSIdentifier ) extends DDSDAbstraction( name, itentifier)  {
  import Group._

  protected val sem = new Semaphore( 0 )

  /**
   * Adds the member to this group.
   */
  def join()

  /**
   * Removes the member from this group.
   */
  def leave()

  /**
   * Returns the group size.
   * @return the currently estimated group size
   */
  def size(): Int

  /**
   * Provides the current group view, i.e. the list of its members.
   *
   * @return the list of group members
   */
  def view(): ( TraversableOnce[UUID], Int )
  /**
   * Waits for a specific view size to be established.
   *
   * @param n number of member that have to join the group
   */

  def waitForViewSize( n: Int ) {
    while ( n != size ) { sem.tryAcquire(  Group.POLL_INTERVAL, Group.TIME_UNIT ) }
  }

  /**
   * Waits for a specific view size to be established or a maximum time to
   * have elapsed.
   *
   * @param n number of member that have to join the group
   * @param timeout time for which the call will wait for the view to establish
   */
  def waitForViewSize( n: Int, timeout: Int ) {

    val stopTime = System.currentTimeMillis() + timeout;
    while ( n != size && System.currentTimeMillis() <= stopTime ) {
      val leftTime = stopTime - System.currentTimeMillis()
      if ( leftTime < Group.POLL_INTERVAL ) {
        sem.tryAcquire( leftTime, Group.TIME_UNIT )
      } else {
        sem.tryAcquire(  Group.POLL_INTERVAL, Group.TIME_UNIT )
      }
    }
  }

}