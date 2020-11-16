package com.riiablo.server.d2gs_test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
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
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;

import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.net.packet.netty.Connection;
import com.riiablo.net.packet.netty.Netty;
import com.riiablo.net.packet.netty.NettyData;
import com.riiablo.onet.Endpoint;
import com.riiablo.onet.EndpointedChannelHandler;
import com.riiablo.onet.PacketProcessor;
import com.riiablo.onet.tcp.TcpEndpoint;

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
  private final ClientData[] clients = new ClientData[MAX_CLIENTS]; {
    for (int i = 0; i < MAX_CLIENTS; i++) clients[i] = new ClientData();
  }

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
                  .addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                      super.channelInactive(ctx);
                      notifyChannelInactive(ctx);
                    }
                  })
                  .addLast(new SizePrefixedDecoder())
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
    processPacket(ctx, from, Netty.getRootAsNetty(bb.nioBuffer()));
  }

  private int state = 0;
  public static final int CLIENT_CONNECTING = 0;
  public static final int CLIENT_DISCONNECTED = 0;
  public static final int CLIENT_CONNECT = 0;

  public void processPacket(ChannelHandlerContext ctx, SocketAddress from, Netty netty) {
    Gdx.app.debug(TAG, "  " + "dataType=" + NettyData.name(netty.dataType()));
    switch (netty.dataType()) {
      case NettyData.Connection:
        Connection(ctx, from, netty);
        break;
      case NettyData.Disconnect:
        Disconnect(ctx, from, netty);
        break;
      default:
        Gdx.app.debug(TAG, "unknown data type: " + netty.dataType());
        Gdx.app.debug(TAG, "  " + "closing " + ctx);
        ctx.close();
    }
  }

  private void Connection(ChannelHandlerContext ctx, SocketAddress from, Netty netty) {
    Gdx.app.debug(TAG, "Connection from " + from);
    Connection connection = (Connection) netty.data(new Connection());

    boolean generateSalt = true;
    long clientSalt = connection.salt();
    Gdx.app.debug(TAG, "  " + String.format("client salt=%016x", clientSalt));

    synchronized (clients) {
      final ClientData client;
      final ClientData[] clients = this.clients;

      int id;
      for (id = 0; id < MAX_CLIENTS && !from.equals(clients[id].address); id++) ;
      if (id == MAX_CLIENTS) {
        Gdx.app.debug(TAG, "  " + "no connection record found for " + from);
        Gdx.app.debug(TAG, "  " + "creating connection record for " + from);

        for (id = 0; id < MAX_CLIENTS && clients[id].connected; id++);
        assert id != MAX_CLIENTS : "no available client slots. connection limiter should have caught this";
        if (id == MAX_CLIENTS) {
          Gdx.app.error(TAG, "  " + "client connected, but no slot is available");
          Gdx.app.debug(TAG, "  " + "closing " + ctx);
          ctx.close();
          return;
        }

        Gdx.app.debug(TAG, "  " + "assigned " + from + " to " + id);
        client = clients[id].connect(ctx.channel(), from, clientSalt);
      } else {
        Gdx.app.debug(TAG, "  " + "found connection record for " + from + " as " + id);
        client = clients[id];
        Gdx.app.debug(TAG, "  " + "checking client salt");
        if (client.clientSalt == clientSalt) {
          Gdx.app.debug(TAG, "  " + "client salt matches server record");
          generateSalt = false;
        } else {
          Gdx.app.debug(TAG, "  " + "client salt mismatch with server record");
          Gdx.app.debug(TAG, "  " + "updating client salt to server record");
          clientSalt = client.clientSalt;
          Gdx.app.debug(TAG, "  " + String.format("client salt=%016x", clientSalt));
        }
      }

      long serverSalt;
      if (generateSalt) {
        Gdx.app.debug(TAG, "  " + "generating server salt");
        if (client.serverSalt != 0L) {
          Gdx.app.debug(TAG, "  " + String.format("overwriting existing server salt %016x", client.serverSalt));
        }
        serverSalt = client.serverSalt = MathUtils.random.nextLong();
        Gdx.app.debug(TAG, "  " + String.format("server salt=%016x", serverSalt));
      } else {
        serverSalt = client.serverSalt;
      }

      long salt = client.xor = clientSalt ^ serverSalt;
      Gdx.app.debug(TAG, "  " + String.format("salt=%016x", salt));
    }
  }

  private void Disconnect(ChannelHandlerContext ctx, SocketAddress from, Netty netty) {
    Gdx.app.debug(TAG, "Disconnect from " + from);
//    Disconnect disconnect = (Disconnect) netty.data(new Disconnect());
    disconnect(ctx, from);
  }

  private void disconnect(ChannelHandlerContext ctx, SocketAddress from) {
    Gdx.app.debug(TAG, "  " + "disconnecting " + from);
    synchronized (clients) {
      int id;
      for (id = 0; id < MAX_CLIENTS && !from.equals(clients[id].address); id++) ;
      if (id == MAX_CLIENTS) {
        Gdx.app.debug(TAG, "  " + "client from " + from + " already disconnected");
      } else {
        Gdx.app.debug(TAG, "  " + "found connection record for " + from + " as " + id);
        Gdx.app.debug(TAG, "  " + "disconnecting " + id);
        clients[id].disconnect();
      }

      Gdx.app.debug(TAG, "  " + "closing " + ctx);
      ctx.close();
    }
  }

  private void notifyChannelInactive(ChannelHandlerContext ctx) {
    Gdx.app.debug(TAG, "notifyChannelInactive");
    SocketAddress from = endpoint.getRemoteAddress(ctx, null);
    disconnect(ctx, from);
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
        Gdx.app.debug(TAG, "  " + "closing " + ctx);
        ctx.close();
        Gdx.app.debug(TAG, String.format("connection closed. maximum concurrent connections reached %d / %d", count, maxClients));
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
    Channel channel;
    SocketAddress address;
    boolean connected;

    ClientData connect(Channel channel, SocketAddress address, long clientSalt) {
      assert !connected;
      this.channel = channel;
      this.address = address;
      this.clientSalt = clientSalt;
      connected = true;
      return this;
    }

    ClientData disconnect() {
      assert connected;
      connected = false;
      channel = null;
      address = null;
      return this;
    }

    @Override
    public String toString() {
      return connected ? String.format("[%016x: %s]", xor, address) : "[disconnected]";
    }
  }

  private static class SizePrefixedDecoder extends ByteToMessageDecoder {
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
  }
}
