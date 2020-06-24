package com.riiablo.net.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import com.riiablo.codec.Animation;
import com.riiablo.net.Endpoint;
import com.riiablo.net.EndpointedChannelHandler;
import com.riiablo.net.PacketProcessor;

public class TestServer extends ApplicationAdapter implements PacketProcessor {
  private static final String TAG = "Server";

  static final int PORT = 6114;

  public static void main(String[] args) throws Exception {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    config.renderInterval = Animation.FRAME_DURATION;
    new HeadlessApplication(new TestServer(), config);
  }

  private Endpoint<ByteBuf, Object> endpoint;

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap()
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              endpoint = new TcpEndpoint(ch, TestServer.this);
              ch.pipeline()
                  .addLast(new EndpointedChannelHandler<>(ByteBuf.class, endpoint))
                  ;
            }
          })
          .option(ChannelOption.SO_BACKLOG, 128)
          .childOption(ChannelOption.SO_KEEPALIVE, true);

      ChannelFuture f = b.bind(PORT).sync();
      f.channel().closeFuture().sync();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
      Gdx.app.exit();
    }
  }

  @Override
  public void render() {
    endpoint.update(Gdx.graphics.getDeltaTime());
  }

  @Override
  public void processPacket(ByteBuf bb) {
    Gdx.app.debug(TAG, "Processing packet...");
    Gdx.app.log(TAG, "  " + ByteBufUtil.hexDump(bb));
  }
}
