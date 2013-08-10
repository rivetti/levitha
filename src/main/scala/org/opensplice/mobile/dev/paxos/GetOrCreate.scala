package org.opensplice.mobile.dev.paxos

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

object GetOrCreate {
  object Default {

    /*implicit def create[T]()( implicit m: scala.reflect.Manifest[T] ): T = {
      m.runtimeClass.newInstance.asInstanceOf[T]
    }*/

    implicit def create[T](): HashSet[T] = {
      new HashSet[T]()
    }

  }
}

trait GetOrCreate[T1, T2] extends HashMap[T1, T2] { //MapLike[T1, T2, GetOrCreate[T1, T2]] {

  def getOrCreate(key: T1)(implicit create: () => T2) = {
    this.getOrElse(key, {
      val itemObj = create()
      this.put(key, itemObj)
      itemObj
    })
  }
}
