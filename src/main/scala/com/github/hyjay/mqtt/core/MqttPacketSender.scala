package com.github.hyjay.mqtt.core

trait MqttPacketSender {

  /**
    * Send a packet asynchronously.
    *
    * @param packet
    */
  def send(packet: Packet): Unit
}