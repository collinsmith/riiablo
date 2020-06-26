package com.riiablo.net.tcp;

import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import com.riiablo.codec.Animation;
import com.riiablo.net.Endpoint;
import com.riiablo.net.EndpointedChannelHandler;
import com.riiablo.net.PacketProcessor;
import com.riiablo.net.packet.netty.Connection;
import com.riiablo.net.packet.netty.Netty;
import com.riiablo.net.packet.netty.NettyData;
import com.riiablo.net.reliable.QoS;

public class TestClient extends ApplicationAdapter implements PacketProcessor {
  private static final String TAG = "Client";

  public static void main(String[] args) throws Exception {
    Thread.sleep(1000);
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    config.renderInterval = Animation.FRAME_DURATION;
    new HeadlessApplication(new TestClient(), config);
  }

  private Endpoint<?> endpoint;
  private EventLoopGroup group;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap()
          .group(group)
          .channel(NioSocketChannel.class)
          .option(ChannelOption.SO_KEEPALIVE, true)
          .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              Endpoint<ByteBuf> endpoint = new TcpEndpoint(ch, TestClient.this);
              TestClient.this.endpoint = endpoint;
              ch.pipeline()
                  .addLast(new EndpointedChannelHandler<>(ByteBuf.class, endpoint))
                  ;
            }
          });

      ChannelFuture f = b.connect("localhost", TestServer.PORT).sync();
      sendPacket();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
      Gdx.app.exit();
    }
  }

  private void sendPacket() {
    InetSocketAddress remoteAddress = (InetSocketAddress) endpoint.channel().remoteAddress();
    Gdx.app.log(TAG, "Sending Connection packet to " + remoteAddress.getHostString() + ":" + remoteAddress.getPort());

    FlatBufferBuilder builder = new FlatBufferBuilder();
    Connection.startConnection(builder);
    int dataOffset = Connection.endConnection(builder);
    int offset = Netty.createNetty(builder, NettyData.Connection, dataOffset);
    Netty.finishNettyBuffer(builder, offset);

    endpoint.sendMessage(QoS.Unreliable, builder.dataBuffer());
  }

  @Override
  public void render() {
    endpoint.update(Gdx.graphics.getDeltaTime());
  }

  @Override
  public void dispose() {
    if (!group.isShuttingDown()) group.shutdownGracefully();
  }

  @Override
  public void processPacket(ChannelHandlerContext ctx, ByteBuf bb) {
    Gdx.app.debug(TAG, "Processing packet...");
    Gdx.app.log(TAG, ByteBufUtil.hexDump(bb));
  }
}
