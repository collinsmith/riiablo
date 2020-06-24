package com.riiablo.net.reliable;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import com.riiablo.codec.Animation;
import com.riiablo.net.PacketProcessor;
import com.riiablo.net.packet.netty.Netty;
import com.riiablo.net.packet.netty.NettyData;

public class TestServer extends ApplicationAdapter implements PacketProcessor {
  private static final String TAG = "Server";

  static final int PORT = 6114;

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    config.renderInterval = Animation.FRAME_DURATION;
    new HeadlessApplication(new TestServer(), config);
  }

  private ReliableEndpoint endpoint;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap()
          .group(group)
          .channel(NioDatagramChannel.class)
          .option(ChannelOption.SO_BROADCAST, true)
          .handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel ch) {
              endpoint = new ReliableEndpoint(ch, TestServer.this);
              ch.pipeline()
                  .addLast(new ReliableChannelHandler(endpoint))
                  ;
            }
          })
          ;

      ChannelFuture f = b.bind(PORT).sync();
      f.channel().closeFuture().sync();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    } finally {
      group.shutdownGracefully();
    }
  }

  @Override
  public void render() {
    if (endpoint != null && endpoint.isConnected()) {
      endpoint.update(Gdx.graphics.getDeltaTime());
    }
  }

  @Override
  public void processPacket(ByteBuf bb) {
    Gdx.app.debug(TAG, "Processing packet...");
    Gdx.app.log(TAG, ByteBufUtil.hexDump(bb));

    ByteBuffer nioBuffer = bb.nioBuffer();
    Netty netty = Netty.getRootAsNetty(nioBuffer);

    byte dataType = netty.dataType();
    if (0 <= dataType && dataType < NettyData.names.length) {
      Gdx.app.debug(TAG, "dataType=" + NettyData.name(dataType));
    }
  }
}
