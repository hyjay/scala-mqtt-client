# scala-mqtt-client
[![Build Status](https://travis-ci.org/hyjay/scala-mqtt-client.svg?branch=master)](https://travis-ci.org/hyjay/scala-mqtt-client)

An asynchronous, reliable and seamless MQTT client in Scala, based on Netty.

## Getting Started
#### MQTT client
With the library you can create a MQTT client, send and receive any MQTT packets as simple as it can be.
```scala
import com.github.hyjay.mqtt.core._
import com.github.hyjay.mqtt.netty.NettyMqttClient

val client  = new NettyMqttClient("localhost", 1883, tls = false)
// Connect, subscribe a topic and publish a message to the topic
val pingpong = for {
  connack <- client.connect(CONNECT("CLIENT_ID"))
  _ = client.send(SUBSCRIBE(Seq(("TOPIC", 0))))
  suback <- client.pull()   
  _ = client.send(PUBLISH("TOPIC", "hello, world!".getBytes.toSeq))
  pub <- client.pull()
} yield pub

val receivedPub = Await.result(pingpong, 3.seconds)
println(s"Got self-published message $receivedPub")
```

#### MQTT actor
Also the library provides a higher abstracted API than MQTT client, MQTT actor. MQTT actor helps developers focus on 
business logic with MQTT but not the MQTT protocol specification. The library handles any work for satisfying 
the MQTT specification behind the scenes, for instance sending PINGREQ periodically.

With the library you can write business logic with MQTT as simple as it can be.
```scala
// A program that publishes a message every minute.
import java.util.concurrent.Executors
import com.github.hyjay.mqtt.core._
import com.github.hyjay.mqtt.actor._

val actor = new MqttActor {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val scheduler = Scheduler()(Executors.newScheduledThreadPool(1))
  
  private def executeEveryMinute(run: () => Unit): Future[Unit] = {
     run()
     scheduler.sleep(1.minute).flatMap(executeEveryMinute(run))
  }
  
  override def onReceived(packet: Packet, packetSender: MqttPacketSender): Unit = packet match {
    case CONNACK(0, _) => 
      // Successfully connected. Starting publishing
      val publishMessage = PUBLISH("TOPIC", "hello, world".getBytes.toSeq)
      executeEveryMinute(() => packetSender.send(publishMessage))
    case _ =>
      // Ignore other messages
  }
}

val connectionConfig = ConnectionConfig("localhost", 1883, tls = false, CONNECT("CLIENT_ID", keepAlive = 30.seconds))

// Run the actor. Return Future[Unit] that ends when the MQTT connection gets disconnected
MqttActor.run(connectionConfig, actor)
```

## Dependencies
Add this to your `build.sbt`:
```
libraryDependencies += "com.github.hyjay" %% "scala-mqtt-client" % Version
```

## Testing
```
sbt test
```

## License
MIT - See LICENSE for more information.