package org.opensplice.mobile.dev.common

import scala.concurrent.duration.FiniteDuration
import akka.actor.Props
import akka.actor.Actor
import java.util.concurrent.TimeUnit
import akka.actor.ActorRef
import akka.actor.ReceiveTimeout
import scala.concurrent.duration.Duration
import org.opensplice.mobile.dev.tools.DALogger

object Timer {
  def apply( timeoutValue: Int, timeUnit: TimeUnit ): Props = Props( new Timer( timeoutValue, timeUnit ) )

  sealed class TimerEvents
  case class Reset() extends TimerEvents
  case class Stop() extends TimerEvents
  case class Start() extends TimerEvents
  case class Timeout() extends TimerEvents
}

class Timer( timeoutValue: Int, timeUnit: TimeUnit ) extends Actor with DALogger {

  logger.debug( "Timer Created" )

  import Timer._

  val timeout = new FiniteDuration( timeoutValue, timeUnit )

  val stoppedTimer: PartialFunction[Any, Unit] = {
    case e:Start => {
      logger.debug( "Start" )
      context.setReceiveTimeout( timeout )
      context.become( runningTimer )
    }
  }

  val runningTimer: PartialFunction[Any, Unit] = {
    case e:Reset => {
      logger.debug( "Reset" )
      context.setReceiveTimeout( timeout )
    }
    case e: Stop => {
      logger.debug( "Stop" )
      context.setReceiveTimeout( Duration.Undefined )
      context.become( stoppedTimer )
    }
    case e:ReceiveTimeout => {
      logger.debug( "ReceiveTimeout" )
      context.setReceiveTimeout( Duration.Undefined )
      context.parent ! Timer.Timeout()
    }
  }

  override def receive = stoppedTimer
}