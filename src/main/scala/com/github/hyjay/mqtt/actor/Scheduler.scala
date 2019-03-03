package com.github.hyjay.mqtt.actor

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

trait Scheduler {

  /**
    * Return Future[Unit] that succeeds after a specific duration.
    *
    * @param duration
    * @return
    */
  def sleep(duration: FiniteDuration): Future[Unit]
}

object Scheduler {

  def apply()(implicit ses: ScheduledExecutorService): Scheduler = new JavaScheduler()

  private class JavaScheduler()(implicit ses: ScheduledExecutorService) extends Scheduler {

    override def sleep(duration: FiniteDuration): Future[Unit] = {
      val promise = Promise[Unit]()
      ses.schedule(() => promise.success(), duration.toSeconds, TimeUnit.SECONDS)
      promise.future
    }
  }
}
