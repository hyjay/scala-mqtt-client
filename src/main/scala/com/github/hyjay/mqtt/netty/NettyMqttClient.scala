package com.github.hyjay.mqtt.netty

import com.github.hyjay.mqtt.core._
import com.github.hyjay.mqtt.util.ConcurrentQueue
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.UnpooledByteBufAllocator
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{Channel, ChannelInitializer}
import io.netty.handler.codec.mqtt._
import io.netty.handler.ssl.{SslContextBuilder, SslHandler}

import scala.concurrent.{ExecutionContext, Future}

class NettyMqttClient(host: String, port: Int, tls: Boolean)(implicit ec: ExecutionContext) extends MqttClient {

  private val eventQueue = ConcurrentQueue[Either[Disconnected, Packet]]()
  @volatile private var channel: Channel = _

  // Setup a Netty channel for this MQTT client
  private val bootstrap = new Bootstrap()
  private val initializer = new ChannelInitializer[SocketChannel]() {
    @throws[Exception]
    override protected def initChannel(sc: SocketChannel): Unit = {
      val cp = sc.pipeline
      if (tls) {
        val sslEngine = SslContextBuilder
          .forClient()
          .build()
          .newEngine(UnpooledByteBufAllocator.DEFAULT)
        cp.addFirst("ssl", new SslHandler(sslEngine))
      }
      cp.addLast(new MqttDecoder())
      cp.addLast(MqttEncoder.INSTANCE)
      cp.addLast(new MqttChannelInboundHandler(eventQueue.put))
    }
  }
  bootstrap.group(globalNioEventLoopGroup)
    .channel(classOf[NioSocketChannel])
    .handler(initializer)

  override def connect(packet: CONNECT)(implicit ec: ExecutionContext): Future[CONNACK] = {
    channel = bootstrap.connect(host, port).sync().channel()
    send(packet)
    def pullUntilConnack(): Future[CONNACK] = for {
      msg <- pull()
      res <- msg match {
        case ack: CONNACK => Future.successful(ack)
        case _ => pullUntilConnack()
      }
    } yield res
    pullUntilConnack()
  }

  override def send(packet: Packet): Unit = {
    channel.writeAndFlush(PacketEncoder.encode(packet))
  }

  override def pull()(implicit ec: ExecutionContext): Future[Packet] = for {
    msg <- eventQueue.take()
    packets = msg match {
      case Left(disconnected) => throw disconnected
      case Right(p) => p
    }
  } yield packets
}
