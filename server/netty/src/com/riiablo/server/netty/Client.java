package com.riiablo.server.netty;

import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import com.riiablo.codec.Animation;
import com.riiablo.net.packet.netty.Connection;
import com.riiablo.net.packet.netty.NettyData;

public class Client extends ApplicationAdapter {
  private static final String TAG = "Client";

  public static void main(String[] args) throws Exception {
    Thread.sleep(1000);
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    config.renderInterval = Animation.FRAME_DURATION;
    new HeadlessApplication(new Client(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    EventLoopGroup group = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap()
          .group(group)
          .channel(NioDatagramChannel.class)
          .handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel ch) {
              final ClientHandler client = new ClientHandler();
              ch.pipeline()
                  .addLast(client)
                  .addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                      client.init(ctx);
                      ctx.pipeline().remove(this);
                    }
                  })
                  ;
            }
          });

      ChannelFuture f = b.connect("localhost", Main.PORT);
      f.channel().closeFuture().sync();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    } finally {
      group.shutdownGracefully();
    }
  }

  public static class ClientHandler extends ReliableChannelHandler {
    ClientHandler() {
    }

    void init(ChannelHandlerContext ctx) {
      InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
      Gdx.app.log(TAG, "Connecting to " + remoteAddress.getHostString() + ":" + remoteAddress.getPort());

      FlatBufferBuilder builder = new FlatBufferBuilder();
      Connection.startConnection(builder);
      int dataOffset = Connection.endConnection(builder);
      createNetty(builder, NettyData.Connection, dataOffset);

      ByteBuf byteBuf = Unpooled.wrappedBuffer(builder.dataBuffer());
      ctx.writeAndFlush(byteBuf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
      ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      Gdx.app.error(TAG, cause.getMessage(), cause);
      ctx.close();
    }
  }
}
