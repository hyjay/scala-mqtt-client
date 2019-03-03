package com.github.hyjay.mqtt.netty

import java.util.UUID

import com.github.hyjay.mqtt._
import com.github.hyjay.mqtt.core._

import scala.concurrent.Await

class NettyMqttClientSpec extends MqttSpec {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  it should "be able to connect to a MQTT broker" in {
    val client = new NettyMqttClient(host, port, tls = false)

    val ack = Await.result(client.connect(CONNECT("CLIENT_ID")), 3.seconds)

    ack shouldBe CONNACK(0, isSessionPresent = false)
  }

  // Replace BROKER_HOST to the host of a MQTT broker that provides TLS connection.
  it should "support tls connection" ignore {
    val client = new NettyMqttClient("BROKER_HOST", 8883, tls = true)

    val ack = Await.result(client.connect(CONNECT(UUID.randomUUID().toString)), 3.seconds)

    ack shouldBe CONNACK(0, isSessionPresent = false)
  }

  it should "send and receive MQTT packets" in {
    // Connect, subscribe and publish a message
    val client = new NettyMqttClient(host, port, tls = false)
    Await.result(client.connect(CONNECT("CLIENT_ID")), 3.seconds)

    client.send(SUBSCRIBE(Seq("TEST_TOPIC" -> 0)))
    Await.result(client.pull(), 3.seconds) shouldBe SUBACK(1)

    client.send(PUBLISH("TEST_TOPIC", "PAYLOAD".getBytes.toSeq))
    Await.result(client.pull(), 3.seconds) shouldBe PUBLISH("TEST_TOPIC", "PAYLOAD".getBytes.toSeq, -1)
  }

  it should "throw Disconnected if the MQTT connection got disconnected" in {
    val client = new NettyMqttClient(host, port, tls = false)
    Await.result(client.connect(CONNECT("CLIENT_ID")), 3.seconds)

    client.send(DISCONNECT())
    the[Disconnected] thrownBy { Await.result(client.pull(), 3.seconds) }

    // Also it should be able to reconnect
    val ack = Await.result(client.connect(CONNECT("CLIENT_ID")), 3.seconds)
    ack shouldBe CONNACK(0, isSessionPresent = false)
  }
}
