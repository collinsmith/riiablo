package com.riiablo.server.d2gs;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;

import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.net.Endpoint;
import com.riiablo.net.EndpointedChannelHandler;
import com.riiablo.net.PacketProcessor;
import com.riiablo.net.packet.netty.Connection;
import com.riiablo.net.packet.netty.Netty;
import com.riiablo.net.packet.netty.NettyData;
import com.riiablo.net.tcp.TcpEndpoint;

public class Main extends ApplicationAdapter implements PacketProcessor {
  private static final String TAG = "D2GS";

  private static final boolean DEBUG = true;

  static final int PORT = 6114;
  private static final int MAX_CLIENTS = Riiablo.MAX_PLAYERS;

  public static void main(String[] args) {
    HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
    config.renderInterval = Animation.FRAME_DURATION;
    new HeadlessApplication(new Main(), config);
  }

  private Endpoint<?> endpoint;
  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  private final ConnectionLimiter connectionLimiter = new ConnectionLimiter(MAX_CLIENTS);

  private final ConcurrentHashMap<SocketAddress, Integer> connectionIds = new ConcurrentHashMap<>(32);
  private final ConcurrentHashMap<SocketAddress, ClientData> clientDatas = new ConcurrentHashMap<>(32);

  @Override
  public void create() {
    Gdx.app.setLogLevel(Application.LOG_DEBUG);

    bossGroup = new NioEventLoopGroup();
    workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap()
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
              TcpEndpoint endpoint = new TcpEndpoint(ch, Main.this);
              Main.this.endpoint = endpoint;
              ch.pipeline()
                  .addFirst(connectionLimiter)
                  .addLast(new ByteToMessageDecoder() {
                    @Override
                    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
                      if (in.readableBytes() < 4) return;
                      in.markReaderIndex();
                      final int length = in.readIntLE();
                      if (in.readableBytes() < length) {
                        in.resetReaderIndex();
                        return;
                      }
                      out.add(in.readRetainedSlice(length));
                    }
                  })
                  .addLast(new EndpointedChannelHandler<>(ByteBuf.class, endpoint))
                  ;
            }
          })
          .option(ChannelOption.SO_BACKLOG, 128)
          .childOption(ChannelOption.TCP_NODELAY, true)
          .childOption(ChannelOption.SO_KEEPALIVE, true);

      ChannelFuture f = b.bind(PORT).sync();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
      Gdx.app.exit();
    }
  }

  @Override
  public void render() {
    if (endpoint == null || !endpoint.channel().isActive()) return;
    endpoint.update(Gdx.graphics.getDeltaTime());
  }

  @Override
  public void dispose() {
    Gdx.app.log(TAG, "Shutting down...");
    if (!workerGroup.isShuttingDown()) workerGroup.shutdownGracefully();
    if (!bossGroup.isShuttingDown()) bossGroup.shutdownGracefully();
  }

  @Override
  public void processPacket(ChannelHandlerContext ctx, SocketAddress from, ByteBuf bb) {
    Gdx.app.debug(TAG, "Processing packet...");
    Gdx.app.debug(TAG, "  " + ByteBufUtil.hexDump(bb));
    processPacket(ctx, Netty.getRootAsNetty(bb.nioBuffer()));
  }

  private int state = 0;
  public static final int CLIENT_CONNECTING = 0;
  public static final int CLIENT_DISCONNECTED = 0;
  public static final int CLIENT_CONNECT = 0;

  public void processPacket(ChannelHandlerContext ctx, Netty netty) {
    Gdx.app.debug(TAG, "  " + "dataType=" + NettyData.name(netty.dataType()));

    InetSocketAddress from = (InetSocketAddress) ctx.channel().remoteAddress();
    switch (netty.dataType()) {
      case NettyData.Connection: {
        Connection(ctx, from, netty);
        break;
      }
      default:
        Gdx.app.debug(TAG, "unknown data type: " + netty.dataType());
        ctx.close();
    }
  }

  private void Connection(ChannelHandlerContext ctx, InetSocketAddress from, Netty netty) {
    Gdx.app.debug(TAG, "Connection from " + from);
    Connection connection = (Connection) netty.data(new Connection());

    boolean generateSalt = true;
    long clientSalt = connection.salt();
    Gdx.app.debug(TAG, "  " + String.format("client salt=%016x", clientSalt));
    ClientData client = clientDatas.get(from);
    if (client != null) {
      long storedClientSalt = client.clientSalt;
      if (storedClientSalt == clientSalt) {
        Gdx.app.debug(TAG, "  " + "client salt matches server record");
        generateSalt = false;
      } else {
        Gdx.app.debug(TAG, "  " + "client salt mismatch with server record");
        Gdx.app.debug(TAG, "  " + "updating client salt to server record");
        client.clientSalt = clientSalt;
        clientSalt = storedClientSalt;
        Gdx.app.debug(TAG, "  " + String.format("client salt=%016x", clientSalt));
      }
    } else {
      Gdx.app.debug(TAG, "  " + "no server record found matching client salt");
      clientDatas.put(from, client = new ClientData(clientSalt));
    }

    long serverSalt;
    if (generateSalt) {
      Gdx.app.debug(TAG, "  " + "generating server salt");
      serverSalt = MathUtils.random.nextLong();
      if (client.serverSalt != 0L) {
        Gdx.app.debug(TAG, "  " + String.format("overwriting existing server salt %016x", client.serverSalt));
      }
      client.serverSalt = serverSalt;
      Gdx.app.debug(TAG, "  " + String.format("server salt=%016x", serverSalt));
    } else {
      serverSalt = client.serverSalt;
    }

    long salt = client.xor = clientSalt ^ serverSalt;
    Gdx.app.debug(TAG, "  " + String.format("salt=%016x", salt));
  }

  @ChannelHandler.Sharable
  static class ConnectionLimiter extends ChannelInboundHandlerAdapter {
    static final String TAG = "ConnectionLimiter";

    final int maxClients;
    final AtomicInteger connections = new AtomicInteger();

    ConnectionLimiter(int maxClients) {
      this.maxClients = maxClients;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      int count = connections.incrementAndGet();
      if (count <= MAX_CLIENTS) {
        Gdx.app.debug(TAG, String.format("connection accepted. %d / %d", count, maxClients));
        super.channelActive(ctx);
      } else {
        ctx.close();
        Gdx.app.debug(TAG, String.format("closing connection. maximum concurrent connections reached %d / %d", count, maxClients));
      }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      super.channelInactive(ctx);
      int count = connections.decrementAndGet();
      Gdx.app.debug(TAG, String.format("connection closed. %d / %d", count, MAX_CLIENTS));
    }
  }

  private static class ClientData {
    long clientSalt;
    long serverSalt;
    long xor;
    byte state;

    ClientData(long clientSalt) {
      this.clientSalt = clientSalt;
    }
  }
}
