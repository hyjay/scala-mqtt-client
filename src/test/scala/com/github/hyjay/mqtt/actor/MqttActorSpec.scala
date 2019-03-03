package com.github.hyjay.mqtt.actor

import java.util.concurrent.Executors

import com.github.hyjay.mqtt.MqttSpec
import com.github.hyjay.mqtt.core._
import com.github.hyjay.mqtt.util.ConcurrentQueue

import scala.concurrent.Await
import scala.concurrent.duration._

class MqttActorSpec extends MqttSpec {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val connectionConfig = ConnectionConfig(
    host,
    port,
    tls = false,
    CONNECT("CLIENT_ID", keepAlive = 3.seconds)
  )

  private implicit val scheduler: Scheduler = Scheduler()(Executors.newScheduledThreadPool(1))

  class FakeActor(disconnectAfter: FiniteDuration = 0.seconds) extends MqttActor {

    val receivedPackets: ConcurrentQueue[Packet] = ConcurrentQueue[Packet]()

    override def onReceived(packet: Packet, packetSender: MqttPacketSender): Unit = packet match {
      case CONNACK(0, _) =>
        receivedPackets.put(packet)
        if (disconnectAfter.toSeconds > 0) {
          scheduler.sleep(disconnectAfter).foreach(_ => packetSender.send(DISCONNECT()))
        }
      case other =>
        receivedPackets.put(other)
    }
  }

  "run" should "initialize a connection to a broker" in {
    val actor = new FakeActor()

    MqttActor.run(connectionConfig, actor)

    Await.result(actor.receivedPackets.take(), 3.seconds) shouldBe CONNACK(0, isSessionPresent = false)
  }

  it should "handle MQTT ping" in {
    val actor = new FakeActor()

    MqttActor.run(connectionConfig, actor)

    Await.result(actor.receivedPackets.take(), 3.seconds) shouldBe CONNACK(0, isSessionPresent = false)
    Await.result(actor.receivedPackets.take(), 5.seconds) shouldBe PINGRESP()
    Await.result(actor.receivedPackets.take(), 5.seconds) shouldBe PINGRESP()
    Await.result(actor.receivedPackets.take(), 5.seconds) shouldBe PINGRESP()
  }

  it should "end when it disconnected" in {
    val actor = new FakeActor(1.seconds)

    val future = MqttActor.run(connectionConfig, actor)

    future.isCompleted shouldBe false
    Thread.sleep(2000)
    future.failed.isCompleted shouldBe true
    Await.result(future.failed, 1.seconds) shouldBe Disconnected()
  }

  "runForever" should "run created actors by actorBuilder forever" in {
    val actor = new FakeActor(1.seconds)

    MqttActor.runForever(connectionConfig, () => actor, 1.seconds)

    val connack = CONNACK(0, isSessionPresent = false)
    Await.result(actor.receivedPackets.take(), 3.seconds) shouldBe connack
    Await.result(actor.receivedPackets.take(), 3.seconds) shouldBe connack
    Await.result(actor.receivedPackets.take(), 3.seconds) shouldBe connack
  }
}
