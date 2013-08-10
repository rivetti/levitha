package org.opensplice.mobile.dev.common

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable.Queue

object DAActor2 {

  def getExecutor(name: String, poolSize: Int = 1) = {
    new DAActorExecutor(name, poolSize)
  }

  def getDummyExecutor(name: String) = DAActor2

  def post(foo: () => Unit) {
    foo()
  }

  class DAActorExecutor(name: String, poolSize: Int) {
    //val queue = new LinkedBlockingQueue[() => Unit]()
    val queue2 = new Queue[() => Unit]()

    /** The capacity bound, or Integer.MAX_VALUE if none */
    val capacity = Integer.MAX_VALUE;

    /** Current number of elements */
    val count = new AtomicInteger(0)
    /** Lock held by take, poll, etc */
    val takeLock = new ReentrantLock()

    /** Wait queue for waiting takes */
    val notEmpty = takeLock.newCondition()

    /** Lock held by put, offer, etc */
    val putLock = new ReentrantLock()

    /** Wait queue for waiting puts */
    val notFull = putLock.newCondition()

    def placeHolder() {}

    def signalNotFull() {
      val putLock = this.putLock;
      putLock.lock();
      try {
        notFull.signal();
      } finally {
        putLock.unlock();
      }
    }

    def take() = {
      var x: () => Unit = placeHolder
      var c = -1;
      val count = this.count;
      val takeLock = this.takeLock;
      takeLock.lockInterruptibly();
      try {
        while (count.get() == 0) {
          notEmpty.await();
        }
        x = queue2.dequeue();
        c = count.getAndDecrement();
        if (c > 1)
          notEmpty.signal();
      } finally {
        takeLock.unlock();
      }
      if (c == capacity)
        signalNotFull();
      x;
    }

    def signalNotEmpty() {
      val takeLock = this.takeLock;
      takeLock.lock();
      try {
        notEmpty.signal();
      } finally {
        takeLock.unlock();
      }
    }

    def put(e: () => Unit) {
      if (e == null) throw new NullPointerException();

      var c = -1
      val putLock = this.putLock;
      val count = this.count;
      putLock.lockInterruptibly();
      try {
        /*
             * Note that count is used in wait guard even though it is
             * not protected by lock. This works because count can
             * only decrease at this point (all other puts are shut
             * out by lock), and we (or some other waiting put) are
             * signalled if it ever changes from capacity. Similarly
             * for all other uses of count in other wait guards.
             */
        while (count.get() == capacity) {
          notFull.await();
        }
        queue2.enqueue(e);
        c = count.getAndIncrement();
        if (c + 1 < capacity)
          notFull.signal();
      } finally {
        putLock.unlock();
      }
      if (c == 0)
        signalNotEmpty();
    }

    val pool = Executors.newFixedThreadPool(1)

    new Thread(new DAActorThread(), name).start()

    def post(foo: () => Unit) {
      put(foo)

    }

    def getScheduleTask(foo: () => Unit) = {
      val thread = new DAActorScheduleTask(foo)
      new Thread(thread).start()
      thread
    }

    class DAActorThread() extends Runnable {

      override def run {
        while (true) {
          val foo = take()
          foo()
        }
      }
    }

    class DAActorScheduleTask(foo: () => Unit) extends Runnable {
      var scheduled = false

      def schedule() {
        if (!scheduled)
          scheduled = true
      }

      override def run {
        while (true) {
          java.util.concurrent.locks.LockSupport.parkNanos(50000)
          if (scheduled) {
            put(foo)
            scheduled = false
          }

        }

      }

    }

  }

}
