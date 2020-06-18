package com.riiablo.server.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.nio.ByteBuffer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.net.packet.netty.Connection;
import com.riiablo.net.packet.netty.Disconnect;
import com.riiablo.net.packet.netty.Header;
import com.riiablo.net.packet.netty.Netty;
import com.riiablo.net.packet.netty.NettyData;
import com.riiablo.net.packet.netty.Ping;

public class Main extends ApplicationAdapter {
  private static final String TAG = "Server";

  static final int PORT = 6114;

  public static void main(String[] args) {
    Options options = new Options()
        .addOption("home", true, "directory containing D2 MPQ files")
        .addOption("seed", true, "seed used to generate map")
        .addOption("diff", true, String.format("game difficulty (%d-%d)", Riiablo.NORMAL, Riiablo.MAX_DIFFS - 1))
        ;

    CommandLine cmd = null;
    try {
      CommandLineParser parser = new DefaultParser();
      cmd = parser.parse(options, args);
    } catch (Throwable t) {
      t.printStackTrace(System.err);
      System.out.println("Failed to start server instance!");
      return;
    }

    // TODO: process args and setup server

    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    config.renderInterval = Animation.FRAME_DURATION;
    new HeadlessApplication(new Main(), config);
  }

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    EventLoopGroup bossGroup = new NioEventLoopGroup();
//    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      Bootstrap b = new Bootstrap()
          .group(bossGroup)
          .channel(NioDatagramChannel.class)
          .option(ChannelOption.SO_BROADCAST, true)
          .handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel ch) {
              ch.pipeline()
                  .addLast(new PacketHandler() {
                    final String TAG = "PacketHandler$1";

                    @Override
                    protected void processPacket(ChannelHandlerContext ctx, Netty netty) {
                      Gdx.app.debug(TAG, "Processing packet...");
                      byte dataType = netty.dataType();
                      if (0 <= dataType && dataType < NettyData.names.length) {
                        Gdx.app.debug(TAG, "dataType=" + NettyData.name(dataType));
                      }
                      switch (dataType) {
                        case NettyData.Connection: {
                          Connection packet = (Connection) netty.data(new Connection());
                          break;
                        }
                        case NettyData.Disconnect: {
                          Disconnect packet = (Disconnect) netty.data(new Disconnect());
                          break;
                        }
                        case NettyData.Ping: {
                          Ping ping = (Ping) netty.data(new Ping());
                          break;
                        }
                        default:
                          if (0 <= dataType && dataType < NettyData.names.length) {
                            Gdx.app.error(TAG, "Ignoring packet /w data type " + dataType + " (" + NettyData.name(dataType) + ")");
                          } else {
                            Gdx.app.error(TAG, "Ignoring packet /w data type " + dataType);
                          }
                      }
                    }
                  })
                  ;
            }
          })
          ;

      ChannelFuture f = b.bind(PORT).sync();
      f.channel().closeFuture().sync();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    } finally {
//      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }

  public static class ServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    ServerHandler() {
      super(false);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      System.out.println("Channel active.");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
      Gdx.app.log(TAG, "Packet from " + msg.sender().getHostName() + ":" + msg.sender().getPort());
      ByteBuf in = msg.content();
      try {
        ByteBuffer buffer = in.nioBuffer();
        Packet packet = Packet.obtain(0, buffer);
        Gdx.app.log(TAG, "  " + NettyData.name(packet.data.dataType()));
        Header header = packet.data.header(new Header());
        Gdx.app.log(TAG, "  " + String.format("SEQ:%d ACK:%d ACK_BITS:%08x", header.sequence(), header.ack(), header.ackBits()));
      } finally {
        in.release(); // TODO: release after packet is processed by server
      }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      System.out.println("Read complete.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      Gdx.app.error(TAG, cause.getMessage(), cause);
      ctx.close();
    }
  }
}
