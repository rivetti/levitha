package org.opensplice.mobile.dev.common

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object DAActor {

  def getExecutor(name: String) = {
    //new DAActorSimpleExecutor(name)
    new DAActorScheduledExecutor2(name)
  }

  def getScheduledExecutor(name: String) = {
    new DAActorScheduledExecutor(name)
  }

  def getScheduledExecutor2(name: String) = {
    new DAActorScheduledExecutor2(name)
  }

  def getSimpleExecutor(name: String) = {
    new DAActorSimpleExecutor(name)
  }

  def getDummyExecutor(name: String) = {
    new DAActorDummyExecutor(name)
  }

  class DAActorScheduledExecutor2(name: String) {
    val queue = new LinkedBlockingQueue[() => Unit]()
    var scheduledTask: () => Unit = null
    val lock: AnyRef = new Object()

    new Thread(new DAActorThread(), name).start()
    new Thread(new DAActorScheduleTask(), name).start()

    def post(foo: () => Unit) {
      queue.put(foo)
    }

    def schedule(foo: () => Unit) {

      if (scheduledTask eq null)
        scheduledTask = foo

    }

    class DAActorThread() extends Runnable {

      override def run {
        while (true) {
          val foo = queue.take
          foo()
        }
      }
    }

    class DAActorScheduleTask() extends Runnable {

      override def run {
        while (true) {
          java.util.concurrent.locks.LockSupport.parkNanos(200000)
          if (scheduledTask ne null) {
            queue.put(scheduledTask)
            scheduledTask = null
          }

        }

      }

    }

  }

  class DAActorScheduledExecutor3(name: String) {
    val queue = new LinkedBlockingQueue[() => Unit]()
    var scheduled = false
    val pool = Executors.newScheduledThreadPool(1)

    new Thread(new DAActorThread(), name).start()

    def post(foo: () => Unit) {
      queue.put(foo)
    }

      
    def schedule(foo: () => Unit) {

      if (!scheduled) {
        scheduled = true
        pool.schedule(new DAActorScheduleTask(foo), 50, TimeUnit.MICROSECONDS)
      }

    }

    class DAActorThread() extends Runnable {

      override def run {
        while (true) {
          val foo = queue.take
          foo()
        }
      }
    }

    class DAActorScheduleTask(foo: () => Unit) extends Runnable {

      override def run {
        queue.put(foo)
        scheduled = false
      }

    }

  }

  class DAActorScheduledExecutor(name: String) {
    val queue = new TimeoutLinkedBlockingQueue[() => Unit]()
    var scheduledTask: () => Unit = null
    val lock: AnyRef = new Object()

    new Thread(new DAActorThread(), name).start()

    def post(foo: () => Unit) {
      queue.put(foo)
    }

    def schedule(foo: () => Unit) {

      if (scheduledTask eq null)
        scheduledTask = foo

    }

    class DAActorThread() extends Runnable {
      val timeout = 50000L
      var lastCheck = System.nanoTime()
      var remainingTimeout = timeout

      override def run {
        while (true) {
          val foo = queue.takeNanos(remainingTimeout)
          if (foo != null)
            foo()

          val currentTime = System.nanoTime()
          remainingTimeout = timeout - (currentTime - lastCheck)

          if (remainingTimeout <= 0) {
            if (scheduledTask ne null) {
              scheduledTask()
              scheduledTask = null
            }
            remainingTimeout = timeout
            lastCheck = currentTime
          }

        }
      }
    }

  }

  class DAActorAgressiveScheduledExecutor(name: String) {
    val queue = new TimeoutLinkedBlockingQueue[() => Unit]()
    var scheduledTask: () => Unit = null
    val lock: AnyRef = new Object()

    new Thread(new DAActorThread(), name).start()

    def post(foo: () => Unit) {
      queue.put(foo)
    }

    def schedule(foo: () => Unit) {

      if (scheduledTask eq null)
        scheduledTask = foo

    }

    class DAActorThread() extends Runnable {
      val timeout = 50000L
      val reducedTimeout = timeout / 4
      var lastExecution = System.nanoTime()
      var remainingTimeout = timeout

      override def run {
        while (true) {
          val foo = queue.takeNanos(remainingTimeout)
          if (foo != null)
            foo()

          val currentTime = System.nanoTime()
          remainingTimeout = timeout - (currentTime - lastExecution)

          if (remainingTimeout <= 0) {
            if (scheduledTask ne null) {
              scheduledTask()
              scheduledTask = null
              lastExecution = currentTime
              remainingTimeout = timeout
            }
            remainingTimeout = reducedTimeout

          }

        }
      }
    }

  }

  class DAActorSimpleExecutor(name: String) {
    val queue = new LinkedBlockingQueue[() => Unit]()
    var scheduledTask: () => Unit = null

    new Thread(new DAActorThread(), name).start()

    def post(foo: () => Unit) {
      queue.put(foo)
    }

    def schedule(foo: () => Unit) {
      foo()
    }

    class DAActorThread() extends Runnable {
      override def run {
        while (true) {
          val foo = queue.take
          foo()
        }
      }
    }

  }

  class DAActorDummyExecutor(name: String) {
    def post(foo: () => Unit) {
      foo()
    }

    def schedule(foo: () => Unit) {
      foo()
    }
  }

}

trait DAActor {
  val lock: AnyRef = new Object()
  import DAActor._

  def !(message: Any) = {
    lock.synchronized(
      receive(message))

  }

  var receive: PartialFunction[Any, Unit]

  def become(newReceive: PartialFunction[Any, Unit]) {
    lock.synchronized(
      receive = newReceive)
  }

}
