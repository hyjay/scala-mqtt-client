package com.github.hyjay.mqtt.util

import java.util.concurrent.Executors

import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._

class SchedulerSpec extends FlatSpec with Matchers {

  "sleep" should "return Future[Unit] that ends after a specific duration" in {
    val scheduler = Scheduler()(Executors.newScheduledThreadPool(1))

    val future = scheduler.sleep(1.seconds)
    future.isCompleted shouldBe false

    Thread.sleep(2000)

    future.isCompleted shouldBe true
  }
}
