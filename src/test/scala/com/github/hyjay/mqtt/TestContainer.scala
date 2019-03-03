package com.github.hyjay.mqtt

import org.scalatest._

/**
  * Testcontainer pattern inspired by https://github.com/testcontainers/testcontainers-scala.
  */
trait ForEachTestContainer extends SuiteMixin { self: Suite =>

  val container: Container

  abstract protected override def runTest(testName: String, args: Args): Status = {
    container.starting()
    try {
      afterStart()
      val status = super.runTest(testName, args)
      status match {
        case FailedStatus => container.failed(new RuntimeException(status.toString))
        case _ => container.succeeded()
      }
      status
    }
    catch {
      case e: Throwable =>
        container.failed(e)
        throw e
    }
    finally {
      try {
        beforeStop()
      }
      finally {
        container.finished()
      }
    }
  }

  def afterStart(): Unit = {}

  def beforeStop(): Unit = {}
}

trait ForAllTestContainer extends SuiteMixin {
  self: Suite =>

  val container: Container

  abstract override def run(testName: Option[String], args: Args): Status = {
    if (expectedTestCount(args.filter) == 0) {
      new CompositeStatus(Set.empty)
    } else {
      container.starting()
      try {
        afterStart()
        super.run(testName, args)
      } finally {
        try {
          beforeStop()
        }
        finally {
          container.finished()
        }
      }
    }
  }

  def afterStart(): Unit = {}

  def beforeStop(): Unit = {}
}

trait Container {
  def finished(): Unit

  def failed(e: Throwable): Unit

  def starting(): Unit

  def succeeded(): Unit
}
