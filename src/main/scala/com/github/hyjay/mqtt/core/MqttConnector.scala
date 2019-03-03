package com.github.hyjay.mqtt.core

import scala.concurrent.{ExecutionContext, Future}

trait MqttConnector {
  
  /**
    * Try connect to a MQTT broker.
    *
    * @throws Disconnected if the client gets disconnected before receiving CONNACK.
    *
    * @param connect
    * @param ec
    * @return
    */
  def connect(connect: CONNECT)(implicit ec: ExecutionContext): Future[CONNACK] 
}