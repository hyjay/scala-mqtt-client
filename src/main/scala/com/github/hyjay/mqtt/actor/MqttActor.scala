package com.github.hyjay.mqtt.actor

import cats.effect.{ContextShift, IO, Timer}
import com.github.hyjay.mqtt.core._
import com.github.hyjay.mqtt.netty.NettyMqttClient
import fs2.Stream
import org.slf4j.LoggerFactory

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

trait MqttActor {

  /**
    * Called after receiving a packet.
    *
    * @param packet
    */
  def onReceived(packet: Packet, packetSender: MqttPacketSender): Unit
}

object MqttActor {

  private val logger = LoggerFactory.getLogger("MqttActor")

  /**
    * Run MqttActor with a given ConnectionConfig. The returned future ends
    * when the MQTT connection gets disconnected.
    *
    * @param config
    * @param actor
    * @param ec
    * @return
    */
  def run(config: ConnectionConfig,
          actor: MqttActor)
         (implicit ec: ExecutionContext,
          scheduler: Scheduler): Future[Unit] = {
    implicit val cs: ContextShift[IO] = IO.contextShift(ec)
    implicit val timer: Timer[IO] = IO.timer(ec)

    val client = new NettyMqttClient(config.host, config.port, config.tls)

    def ping(sender: MqttPacketSender): fs2.Stream[IO, Packet] =
      if (config.connectPacket.keepAlive.toSeconds == 0)
        fs2.Stream.empty
      else
        fs2.Stream.awakeDelay[IO](config.connectPacket.keepAlive).map(_ => PINGREQ())

    def send(client: MqttClient): Stream[IO, Unit] =
      ping(client).through(_.evalMap(p => IO(client.send(p))))

    for {
      connack <- client.connect(config.connectPacket)
      inbound = (Stream.emit(connack) ++ Stream.repeatEval(IO.fromFuture(IO(client.pull)))) merge send(client).drain    // TODO
      _ <- inbound.through(_.evalMap(p => IO(actor.onReceived(p, client)))).compile.drain.unsafeToFuture()
    } yield ()
  }

  /**
    * Run actors by actorBuilder with a given ConnectionConfig forever.
    *
    * @param config
    * @param actorBuilder
    * @param ec
    * @return
    */
  def runForever(config: ConnectionConfig,
                 actorBuilder: () => MqttActor,
                 backoff: FiniteDuration)
                (implicit ec: ExecutionContext,
                 scheduler: Scheduler): Future[Unit] = {
    def go(): Future[Unit] = {
      run(config, actorBuilder())
        .recoverWith {
          case err: Disconnected =>
            logger.info("client disconnected, retrying...")
            scheduler.sleep(backoff).flatMap(_ => go())
          case err: Throwable =>
            logger.error(s"run finished by ${err.getMessage}, retrying...", err)
            scheduler.sleep(backoff).flatMap(_ => go())
        }
    }
    go()
  }
}
