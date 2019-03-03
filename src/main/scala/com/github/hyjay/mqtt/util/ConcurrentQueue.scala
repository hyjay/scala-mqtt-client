package com.github.hyjay.mqtt.util

import cats.effect.IO
import fs2.concurrent.Queue

import scala.concurrent.{ExecutionContext, Future}

private[mqtt] trait ConcurrentQueue[A] {

  /**
    * Put an element.
    *
    * @param a
    */
  def put(a: A): Unit

  /**
    * Return an element immediately. Return None if the queue is empty.
    *
    * @return
    */
  def poll(): Option[A]

  /**
    * Return an element. Wait until an element comes if the queue is empty.
    * @return
    */
  def take(): Future[A]
}

private[mqtt] object ConcurrentQueue {

  def apply[A]()(implicit ec: ExecutionContext): ConcurrentQueue[A] = new Fs2SynchronizedQueue()

  private class Fs2SynchronizedQueue[A]()(implicit ec: ExecutionContext) extends ConcurrentQueue[A] {

    private implicit val cs = IO.contextShift(ec)
    private val q = Queue.unbounded[IO, A].unsafeRunSync()

    override def put(a: A): Unit = {
      q.enqueue1(a).unsafeRunSync()
    }

    override def poll(): Option[A] = {
      q.tryDequeue1.unsafeRunSync()
    }

    override def take(): Future[A] = {
      q.dequeue1.unsafeToFuture()
    }
  }
}
