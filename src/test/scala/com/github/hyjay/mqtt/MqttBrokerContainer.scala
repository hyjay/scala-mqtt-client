package com.github.hyjay.mqtt

import java.util.Properties

import io.moquette.broker.Server
import io.moquette.broker.config.MemoryConfig

object MqttBrokerContainer {

  def apply(host: String, port: Int): Container = new Container {
    private val conf = new MemoryConfig(new Properties())
    conf.setProperty("‘host", host)
    conf.setProperty("port", port.toString)
    private val server = new Server()

    override def finished(): Unit = {
      server.stopServer()
    }

    override def failed(e: Throwable): Unit = {}

    override def starting(): Unit = {
      server.startServer(conf)
    }

    override def succeeded(): Unit = {}
  }
}