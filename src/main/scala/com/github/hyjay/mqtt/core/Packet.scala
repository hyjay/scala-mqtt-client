package com.github.hyjay.mqtt.core

import scala.concurrent.duration._

sealed trait Packet

case class CONNACK(returnCode: Int, isSessionPresent: Boolean) extends Packet

case class CONNECT(clientId: String,
                   isCleanSession: Boolean = true,
                   keepAlive: FiniteDuration = 60.seconds,
                   username: Option[String] = None,
                   password: Option[String] = None) extends Packet

case class DISCONNECT() extends Packet

case class PINGREQ() extends Packet

case class PINGRESP() extends Packet

case class PUBACK(qos: Int, messageId: Int) extends Packet

case class PUBLISH(topic: String,
                   payload: Seq[Byte],
                   packetId: Int = 1,
                   isDup: Boolean = false,
                   qos: Int = 0,
                   isRetain: Boolean = false) extends Packet

case class SUBACK(messageId: Int) extends Packet

case class SUBSCRIBE(topic: Seq[(String, Int)], msgId: Int = 1) extends Packet

case class Unknown(rawMessage: String) extends Packet