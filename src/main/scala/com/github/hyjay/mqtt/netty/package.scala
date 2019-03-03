package com.github.hyjay.mqtt

import io.netty.channel.nio.NioEventLoopGroup

/**
  * package netty implements MqttClient interface via netty framework.
  */
package object netty {

  private[netty] val globalNioEventLoopGroup = new NioEventLoopGroup()

  Runtime.getRuntime.addShutdownHook(new Thread(() => {
    globalNioEventLoopGroup.shutdownGracefully()
  }))
}
