package com.github.hyjay.mqtt.netty

import com.github.hyjay.mqtt.core.{Disconnected, Packet}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.mqtt.MqttMessage
import org.slf4j.LoggerFactory

private[netty] class MqttChannelInboundHandler(notifyEvent: Either[Disconnected, Packet] => Unit)
  extends SimpleChannelInboundHandler[MqttMessage] {

  private val logger = LoggerFactory.getLogger(getClass)

  override def channelUnregistered(ctx: ChannelHandlerContext): Unit = {
    notifyEvent(Left(Disconnected()))
  }

  override def channelRead0(ctx: ChannelHandlerContext, msg: MqttMessage): Unit = {
    notifyEvent(Right(PacketDecoder.decode(msg)))
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.error("error in MQTT netty channel inbound handler", new RuntimeException(cause))
    ctx.disconnect()
  }

}
