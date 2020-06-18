package com.riiablo.server.netty;

import com.google.flatbuffers.ByteBufferUtil;
import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import com.riiablo.codec.Animation;
import com.riiablo.net.packet.netty.Connection;
import com.riiablo.net.packet.netty.Header;
import com.riiablo.net.packet.netty.Netty;
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
              ReliableInboundHandler in = new ReliableInboundHandler();
              ReliableOutboundHandler out = new ReliableOutboundHandler();
              final EchoClientHandler echo = new EchoClientHandler();
              ch.pipeline()
                  .addLast(in)
                  .addLast(echo)
                  .addLast(out)
                  .addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                      echo.init(ctx.channel());
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

  public static class EchoClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    EchoClientHandler() {
      super(false);
    }

    void init(Channel ch) {
      InetSocketAddress remoteAddress = (InetSocketAddress) ch.remoteAddress();
      Gdx.app.log(TAG, "Connecting to " + remoteAddress.getHostString() + ":" + remoteAddress.getPort());

      FlatBufferBuilder builder = new FlatBufferBuilder();
      int headerOffset = Header.createHeader(builder, -1, -1, 0);
      Connection.startConnection(builder);
      int dataOffset = Connection.endConnection(builder);
      int offset = Netty.createNetty(builder, headerOffset, NettyData.Connection, dataOffset);
      Netty.finishSizePrefixedNettyBuffer(builder, offset);

      sanity(builder.dataBuffer());

      ByteBuf byteBuf = Unpooled.wrappedBuffer(builder.dataBuffer());
      ch.writeAndFlush(byteBuf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
      ctx.fireChannelRead(msg);
    }

    private void sanity(ByteBuffer buffer) {
      ByteBuffer tmp = ByteBufferUtil.removeSizePrefix(buffer);
      Netty netty = Netty.getRootAsNetty(tmp);
      Gdx.app.log(TAG, "  " + NettyData.name(netty.dataType()));
      Header header = netty.header(new Header());
      Gdx.app.log(TAG, "  " + String.format("SEQ:%d ACK:%d ACK_BITS:%08x", header.sequence(), header.ack(), header.ackBits()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      Gdx.app.error(TAG, cause.getMessage(), cause);
      ctx.close();
      ctx.fireExceptionCaught(cause);
    }
  }
}
