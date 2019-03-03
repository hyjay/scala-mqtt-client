package com.github.hyjay.mqtt.netty

import com.github.hyjay.mqtt.core._
import io.netty.handler.codec.mqtt._

private[netty] trait PacketDecoder {

  def decode(message: MqttMessage): Packet =
    message match {
      case connack: MqttConnAckMessage =>
        val header = connack.variableHeader()
        CONNACK(header.connectReturnCode().byteValue(), header.isSessionPresent)

      case suback: MqttSubAckMessage =>
        val header = suback.variableHeader()
        SUBACK(header.messageId())

      case publish: MqttPublishMessage =>
        val header = publish.variableHeader
        val payload = publish.payload()
        val bytes = new Array[Byte](payload.readableBytes())
        payload.getBytes(payload.readerIndex(), bytes)
        PUBLISH(header.topicName(), bytes.toSeq, packetId = header.packetId(), qos = publish.fixedHeader().qosLevel().value())

      case puback: MqttPubAckMessage =>
        val header = puback.variableHeader()
        PUBACK(puback.fixedHeader().qosLevel().value(), header.messageId())

      case pingresp: MqttMessage if pingresp.fixedHeader().messageType() == MqttMessageType.PINGRESP =>
        PINGRESP()

      case _ =>
        Unknown(message.toString)
    }
}

private[netty] object PacketDecoder extends PacketDecoder
