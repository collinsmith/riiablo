package com.riiablo.server.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
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
        .addOption("diff", true, String.format("game difficulty (%d-%d)", Riiablo.NORMAL, Riiablo.NUM_DIFFS - 1))
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
    config.updatesPerSecond = (int) Animation.FRAMES_PER_SECOND;
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
                  .addLast(new ServerHandler())
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

  public static class ServerHandler extends ReliableChannelHandler {
    private static final String TAG = "ServerHandler";

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

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      Gdx.app.error(TAG, cause.getMessage(), cause);
      ctx.close();
    }
  }
}
