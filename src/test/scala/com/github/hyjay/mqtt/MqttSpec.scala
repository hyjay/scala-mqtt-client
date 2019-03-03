package com.github.hyjay.mqtt

import org.scalatest.{FlatSpec, Matchers}

class MqttSpec extends FlatSpec with Matchers with ForEachTestContainer {

  protected val host: String = "localhost"
  protected val port: Int = 1883

  override val container: Container = MqttBrokerContainer(host, port)
}
