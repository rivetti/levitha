package org.opensplice.mobile.dev.group.stable

import scala.collection.mutable.HashSet
import java.util.UUID
import org.opensplice.mobile.dev.common.toShortString

class TestHashSet extends HashSet[UUID] {
  self =>

  override def toString(): String = {
      def addString( b: StringBuilder, start: String, sep: String, end: String ): StringBuilder = {
        var first = true

        b append start
        for ( x <- self ) {
          if ( first ) {
            b append toShortString( x )
            first = false
          } else {
            b append sep
            b append toShortString( x )
          }
        }
        b append end

        b
      }

    addString( new StringBuilder(), "Set(", ",", ")" ).toString

  }

  override def clone() = new TestHashSet ++= this
}