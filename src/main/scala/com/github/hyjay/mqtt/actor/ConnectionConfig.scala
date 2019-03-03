package com.github.hyjay.mqtt.actor

import com.github.hyjay.mqtt.core.CONNECT

case class ConnectionConfig(host: String, port: Int, tls: Boolean, connectPacket: CONNECT)
