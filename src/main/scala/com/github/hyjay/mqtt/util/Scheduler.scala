package com.github.hyjay.mqtt.util

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise}

private[mqtt] trait Scheduler {

  /**
    * Return Future[Unit] that succeeds after a specific duration.
    *
    * @param duration
    * @return
    */
  def sleep(duration: FiniteDuration): Future[Unit]
}

private[mqtt] object Scheduler {

  def apply()(implicit ses: ScheduledExecutorService): Scheduler = new JavaScheduler()

  private class JavaScheduler()(implicit ses: ScheduledExecutorService) extends Scheduler {

    override def sleep(duration: FiniteDuration): Future[Unit] = {
      val promise = Promise[Unit]()
      ses.schedule(() => promise.success(), duration.toSeconds, TimeUnit.SECONDS)
      promise.future
    }
  }
}
