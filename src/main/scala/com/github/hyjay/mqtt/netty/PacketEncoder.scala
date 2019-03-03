package com.github.hyjay.mqtt.netty

import com.github.hyjay.mqtt.core._
import io.netty.buffer.Unpooled
import io.netty.handler.codec.mqtt._
import org.slf4j.LoggerFactory

private[netty] trait PacketEncoder {

  private val logger = LoggerFactory.getLogger(getClass)

  def encode(packet: Packet): MqttMessage = {
    val protocol = MqttVersion.MQTT_3_1_1

    packet match {
      case connect: CONNECT =>
        val header0 = new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val header1 = new MqttConnectVariableHeader(
          protocol.protocolName(),
          protocol.protocolLevel(),
          connect.username.isDefined,
          connect.password.isDefined,
          false,
          0,
          false,
          connect.isCleanSession,
          connect.keepAlive.toSeconds.toInt
        )
        val payload = new MqttConnectPayload(connect.
          clientId,
          null,
          "",
          connect.username.orNull,
          connect.password.getOrElse("")
        )
        new MqttConnectMessage(header0, header1, payload)

      case connack: CONNACK =>
        val header0 = new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0)
        val header1 = new MqttConnAckVariableHeader(
          MqttConnectReturnCode.valueOf(connack.returnCode.toByte),
          connack.isSessionPresent
        )
        new MqttConnAckMessage(header0, header1)

      case disconnect: DISCONNECT =>
        val header = new MqttFixedHeader(
          MqttMessageType.DISCONNECT,
          false,
          MqttQoS.AT_MOST_ONCE,
          false,
          0
        )
        new MqttMessage(header)

      case pingreq: PINGREQ =>
        val header = new MqttFixedHeader(
          MqttMessageType.PINGREQ,
          false,
          MqttQoS.AT_MOST_ONCE,
          false,
          0
        )
        new MqttMessage(header)

      case pingresp: PINGRESP =>
        val header = new MqttFixedHeader(
          MqttMessageType.PINGRESP,
          false,
          MqttQoS.AT_MOST_ONCE,
          false,
          0
        )
        new MqttMessage(header)

      case puback: PUBACK =>
        val fixedHeader = new MqttFixedHeader(
          MqttMessageType.PUBACK,
          false,
          MqttQoS.valueOf(puback.qos),
          false,
          2
        )
        val variableHeader = MqttMessageIdVariableHeader.from(puback.messageId)
        new MqttPubAckMessage(fixedHeader, variableHeader)

      case publish: PUBLISH =>
        val header0 = new MqttFixedHeader(
          MqttMessageType.PUBLISH,
          publish.isDup,
          MqttQoS.valueOf(publish.qos),
          publish.isRetain,
          0
        )
        val header1 = new MqttPublishVariableHeader(publish.topic, publish.packetId)
        new MqttPublishMessage(header0, header1, Unpooled.wrappedBuffer(publish.payload.toArray))

      case suback: SUBACK =>
        val fixedHeader = new MqttFixedHeader(
          MqttMessageType.SUBACK,
          false,
          MqttQoS.valueOf(0),
          false,
          0
        )
        val variableHeader = MqttMessageIdVariableHeader.from(suback.messageId)
        val payload = new MqttSubAckPayload(suback.messageId)
        new MqttSubAckMessage(fixedHeader, variableHeader, payload)

      case subscribe: SUBSCRIBE =>
        import scala.collection.JavaConverters._

        val header0 = new MqttFixedHeader(
          MqttMessageType.SUBSCRIBE,
          false,
          MqttQoS.AT_LEAST_ONCE,
          false,
          0
        )
        val header1 = MqttMessageIdVariableHeader.from(subscribe.msgId)
        val payload = new MqttSubscribePayload(subscribe.topic.map {
          case (topic, qos) => new MqttTopicSubscription(topic, MqttQoS.valueOf(qos))
        }.asJava)
        new MqttSubscribeMessage(header0, header1, payload)

      case msg: Unknown =>
        val err = new RuntimeException(s"Cannot encode $msg")
        logger.error(s"error in encoding packet to netty MQTT message", err)
        throw err
    }
  }
}

private[netty] object PacketEncoder extends PacketEncoder