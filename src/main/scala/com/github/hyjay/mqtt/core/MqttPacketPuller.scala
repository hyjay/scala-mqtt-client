package com.github.hyjay.mqtt.core

import scala.concurrent.{ExecutionContext, Future}

trait MqttPacketPuller {
  
  /**
    * Pull a received packet. Wait until a packet comes if the queue is empty.
    *
    * @throws Disconnected if the client gets disconnected before receiving a packet.
    *
    * @param ec
    * @return
    */
  def pull()(implicit ec: ExecutionContext): Future[Packet]
}