package com.riiablo.server.d2gs;

import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import com.artemis.utils.BitVector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.engine.Engine;
import com.riiablo.map.Map;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.Disconnect;
import com.riiablo.net.packet.d2gs.Ping;
import com.riiablo.nnet.Endpoint;
import com.riiablo.nnet.PacketProcessor;
import com.riiablo.nnet.tcp.TcpEndpoint;
import com.riiablo.save.CharData;
import com.riiablo.util.DebugUtils;

public class Server implements PacketProcessor {
  private static final String TAG = "D2GS";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_RECEIVED_CACHE = DEBUG && !true;
  private static final boolean DEBUG_RECEIVED_PACKETS = DEBUG && true;
  private static final boolean DEBUG_SENT_PACKETS = DEBUG && true;

  static final int MAX_CLIENTS = Main.MAX_CLIENTS;
  static final int INVALID_CLIENT = -1;

  private final Main main;
  private final InetAddress address;
  private final int port;
  private final D2GSPacketProcessor packetProcessor;

  private ChannelFuture future;
  private ServerBootstrap bootstrap;

  private Endpoint.IdResolver<?> idResolver;
  private Endpoint<?> endpoint;

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  private final ConnectionLimiter connectionLimiter = new ConnectionLimiter(MAX_CLIENTS);
  private final ChannelInboundHandler connectionListener = new ChannelInboundHandlerAdapter() {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      super.channelInactive(ctx);
      notifyChannelInactive(ctx);
    }
  };

  private int connected;
  private final ObjectIntMap<SocketAddress> ids = new ObjectIntMap<>(32);
  private final ClientData[] clients = new ClientData[MAX_CLIENTS]; {
    for (int i = 0; i < MAX_CLIENTS; i++) clients[i] = new ClientData();
    idResolver = new Endpoint.IdResolver<Channel>() {
      @Override
      public Channel get(int id) {
        return clients[id].channel;
      }

      @Override
      public String toString() {
        return ArrayUtils.toString(clients);
      }
    };
  }

  final BlockingQueue<D2GSPacket> inPackets = new ArrayBlockingQueue<>(32);
  final Collection<D2GSPacket> cache = new ArrayList<>(1024);
  final BlockingQueue<D2GSPacket> outPackets = new ArrayBlockingQueue<>(1024);
  static final BitVector ignoredPackets = new BitVector(D2GSData.names.length) {{
    set(D2GSData.EntitySync);
  }};

  final IntIntMap player = new IntIntMap();

  // TODO: Refactor required data from main to ServerConfig or EngineConfig class and pass that instead
  public Server(Main main, InetAddress address, int port, D2GSPacketProcessor packetProcessor) {
    this.main = main;
    this.address = address;
    this.port = port;
    this.packetProcessor = packetProcessor;
  }

  public ChannelFuture future() {
    return future;
  }

  public Endpoint.IdResolver resolver() {
    return idResolver;
  }

  @SuppressWarnings("unchecked")
  private static Endpoint<?> createEndpoint(Endpoint.IdResolver<?> idResolver, PacketProcessor packetProcessor) {
    return new TcpEndpoint((Endpoint.IdResolver<Channel>) idResolver, packetProcessor);
  }

  @SuppressWarnings("unchecked")
  private static <T> EndpointedChannelHandler<T> createChannelHandler(Class<T> packetType, Endpoint<?> endpoint) {
    return new EndpointedChannelHandler<>(packetType, (Endpoint<T>) endpoint);
  }

  public void create() {
    Validate.validState(bossGroup == null);
    Validate.validState(workerGroup == null);
    Validate.validState(bootstrap == null);

    endpoint = createEndpoint(idResolver, this);
    bossGroup = new NioEventLoopGroup();
    workerGroup = new NioEventLoopGroup();
    bootstrap = new ServerBootstrap()
        .group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) {
            Gdx.app.debug(TAG, "initChannel " + ch);
            ch.pipeline()
                .addFirst(connectionLimiter)
                .addLast(connectionListener)
                .addLast(new SizePrefixedDecoder())
                .addLast(createChannelHandler(ByteBuf.class, endpoint))
                ;
          }
        })
        .option(ChannelOption.SO_BACKLOG, 128)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.SO_KEEPALIVE, true)
//        .localAddress(address, port) // FIXME disable binding to LAN IP in favor of localhost
        ;
  }

  public ChannelFuture start() {
    Gdx.app.log(TAG, "Starting server...");
    Validate.validState(future == null);
    Gdx.app.debug(TAG, "attempting to bind to " + bootstrap.config().localAddress());
    future = bootstrap.bind(port);
    future.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        Gdx.app.log(TAG, "successfully bound to " + future.channel().localAddress());
      }
    });
    return future;
  }

  public void dispose() {
    try {
      Gdx.app.debug(TAG, "shutting down channel...");
      future.channel().close();
      future.channel().closeFuture().syncUninterruptibly();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    try {
      Gdx.app.debug(TAG, "shutting down workerGroup...");
      workerGroup.shutdownGracefully().syncUninterruptibly();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    try {
      Gdx.app.debug(TAG, "shutting down bossGroup...");
      bossGroup.shutdownGracefully().syncUninterruptibly();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }

  @Override
  public void processPacket(ChannelHandlerContext ctx, SocketAddress from, ByteBuf bb) {
//    Gdx.app.debug(TAG, "Queueing packet...");
//    Gdx.app.debug(TAG, "  " + ByteBufUtil.hexDump(bb));
    D2GSPacket packet = D2GSPacket.obtain(ctx, from, bb);
    boolean success = false;
    try {
      success = inPackets.offer(packet, 1, TimeUnit.MILLISECONDS);
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
      success = false;
    } finally {
      if (success) {
        ReferenceCountUtil.retain(bb);
      } else {
        Gdx.app.error(TAG, "failed to add packet " + packet + " from " + from + " to queue ");
        Gdx.app.debug(TAG, "  " + "closing " + ctx);
        ctx.close();
      }
    }
  }

  public void updateIncoming(float delta) {
    cache.clear();
    int cached = inPackets.drainTo(cache);
    if (DEBUG_RECEIVED_CACHE && cached > 0) Gdx.app.debug(TAG, "processing " + cached + " packets...");
    for (D2GSPacket packet : cache) {
      if (DEBUG_RECEIVED_PACKETS && !ignoredPackets.get(packet.data.dataType())) Gdx.app.debug(TAG, "processing " + packet + " packet from " + packet.from);
      try {
        packet.id = ids.get(packet.from, INVALID_CLIENT);
        if (packet.id == INVALID_CLIENT && packet.dataType != D2GSData.Connection) {
          Gdx.app.error(TAG, "  " + packet + "from invalid client and not a connection request");
          continue;
        }
        processPacket(packet);
      } finally {
        ReferenceCountUtil.release(packet.bb);
      }
    }
  }

  public void updateOutgoing(float delta) {
    cache.clear();
    outPackets.drainTo(cache);
    for (D2GSPacket packet : cache) {
      if (DEBUG_SENT_PACKETS && !ignoredPackets.get(packet.dataType)) Gdx.app.debug(TAG, "dispatching " + packet + " packet to " + String.format("0x%08X", packet.id));
      for (int i = 0, flag = 1; i < MAX_CLIENTS; i++, flag <<= 1) {
        if ((packet.id & flag) == flag && ((connected & flag) == flag || packet.dataType == D2GSData.Connection)) {
          ClientData client = clients[i];
          if (!client.connected) continue;
          try {
            if (DEBUG_SENT_PACKETS && !ignoredPackets.get(packet.dataType)) Gdx.app.debug(TAG, "  " + "dispatching packet to " + i);
            sendMessage(i, client, packet);
          } catch (Throwable t) {
            Gdx.app.error(TAG, t.getMessage(), t);
          }
        }
      }
    }
  }

  private void sendMessage(int id, ClientData client, D2GSPacket packet) {
    packet.buffer.mark();
    endpoint.sendMessage(id, packet.buffer, -1);
    packet.buffer.reset();
    if ((connected & (1 << id)) == 0 && packet.dataType == D2GSData.Connection) {
      connected |= (1 << id);
    }
  }

  public void processPacket(D2GSPacket packet) {
    final D2GS d2gs = packet.data;
    byte dataType = d2gs.dataType();
    if (!ignoredPackets.get(dataType)) {
      String name = dataType < D2GSData.names.length ? D2GSData.name(dataType) : "null";
      Gdx.app.debug(TAG, "  " + String.format("dataType=%s (0x%02x)", name, dataType));
    }
    switch (d2gs.dataType()) {
      case D2GSData.Connection:
        Connection(packet.ctx, packet.from, packet);
        break;
      case D2GSData.Disconnect:
        Disconnect(packet.ctx, packet.from, packet);
        break;
      case D2GSData.Ping:
        Ping(packet.ctx, packet.from, packet);
        break;
      default:
//        Gdx.app.debug(TAG, "  " + "not connection-related. propagating to " + packetProcessor);
        packetProcessor.processPacket(packet);
    }
  }

  private void Connection(ChannelHandlerContext ctx, SocketAddress from, D2GSPacket packet) {
    Connection connection = (Connection) packet.data.data(new Connection());
    String charName = connection.charName();
    int charClass = connection.charClass();
    Gdx.app.log(TAG, "Connection from " + from + " : " + charName);

    int id;
    final ClientData client;
    synchronized (clients) {
      final ClientData clients[] = this.clients;

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
        client = clients[id].connect(ctx.channel(), from, 0);
        ids.put(from, id);
      } else {
        Gdx.app.debug(TAG, "  " + "found connection record for " + from + " as " + id);
        client = clients[id];
      }
    }

    byte[] cofComponents = new byte[16];
    connection.cofComponentsAsByteBuffer().get(cofComponents);
    Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(cofComponents));

    byte[] cofAlphas = new byte[16];
    connection.cofAlphasAsByteBuffer().get(cofAlphas);
    Gdx.app.log(TAG, "  " + Arrays.toString(cofAlphas));
    Gdx.app.log(TAG, "  >" + Arrays.toString(com.riiablo.util.ArrayUtils.toFloatingPoint(cofAlphas)));

    byte[] cofTransforms = new byte[16];
    connection.cofTransformsAsByteBuffer().get(cofTransforms);
    Gdx.app.log(TAG, "  " + DebugUtils.toByteArray(cofTransforms));

    ByteBuffer d2sData = connection.d2sAsByteBuffer();
    CharData charData = CharData.loadFromBuffer(main.diff, d2sData);
    Gdx.app.log(TAG, "  " + charData);

    Vector2 origin = main.map.find(Map.ID.TOWN_ENTRY_1);
    if (origin == null) origin = main.map.find(Map.ID.TOWN_ENTRY_2);
    if (origin == null) origin = main.map.find(Map.ID.TP_LOCATION);
    int entityId = main.factory.createPlayer(charData, origin);
    player.put(id, entityId);
    Gdx.app.log(TAG, "  entityId=" + entityId);

    FlatBufferBuilder builder = new FlatBufferBuilder();
    Connection.startConnection(builder);
    Connection.addEntityId(builder, entityId);
    int connectionOffset = Connection.endConnection(builder);
    int offset = D2GS.createD2GS(builder, D2GSData.Connection, connectionOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, offset);
    D2GSPacket response = D2GSPacket.obtain(1 << id, D2GSData.Connection, builder.dataBuffer());
    outPackets.offer(response);

    Synchronize(id, entityId);

    BroadcastConnect(id, connection, charData, entityId);
  }

  // TODO intention is to prepare a larger reliable sync packet with world state
  private void Synchronize(int id, int entityId) {}

  private void BroadcastConnect(int id, Connection connection, CharData charData, int entityId) {
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int charNameOffset = builder.createString(charData.name);

    byte[] components = new byte[16];
    connection.cofComponentsAsByteBuffer().get(components);
    int componentsOffset = Connection.createCofComponentsVector(builder, components);

    byte[] alphas = new byte[16];
    connection.cofAlphasAsByteBuffer().get(alphas);
    int alphasOffset = Connection.createCofAlphasVector(builder, alphas);

    byte[] transforms = new byte[16];
    connection.cofTransformsAsByteBuffer().get(transforms);
    int transformsOffset = Connection.createCofTransformsVector(builder, transforms);

    Connection.startConnection(builder);
    Connection.addEntityId(builder, entityId);
    Connection.addCharClass(builder, charData.charClass);
    Connection.addCharName(builder, charNameOffset);
    Connection.addCofComponents(builder, componentsOffset);
    Connection.addCofAlphas(builder, alphasOffset);
    Connection.addCofTransforms(builder, transformsOffset);
    int connectionOffset = Connection.endConnection(builder);
    int offset = D2GS.createD2GS(builder, D2GSData.Connection, connectionOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, offset);

    D2GSPacket broadcast = D2GSPacket.obtain(~(1 << id), D2GSData.Connection, builder.dataBuffer());
    boolean success = outPackets.offer(broadcast);
    assert success;
  }

  private void Disconnect(ChannelHandlerContext ctx, SocketAddress from, D2GSPacket packet) {
    Gdx.app.debug(TAG, "Disconnect from " + from);
    Disconnect disconnect = (Disconnect) packet.data.data(new Disconnect());
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
        connected &= ~(1 << id);
        ids.remove(from, INVALID_CLIENT);
        Disconnect(id);
      }

      Gdx.app.debug(TAG, "  " + "closing " + ctx);
      ctx.close();
    }
  }

  private void Disconnect(int id) {
    int entityId = player.get(id, Engine.INVALID_ENTITY);
    assert entityId != Engine.INVALID_ENTITY;
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int disconnectOffset = Disconnect.createDisconnect(builder, entityId);
    int offset = D2GS.createD2GS(builder, D2GSData.Disconnect, disconnectOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, offset);
    D2GSPacket broadcast = D2GSPacket.obtain(~(1 << id), D2GSData.Connection, builder.dataBuffer());
    outPackets.offer(broadcast);

    main.world.delete(entityId);
    player.remove(id, Engine.INVALID_ENTITY);
  }

  private void Ping(ChannelHandlerContext ctx, SocketAddress from, D2GSPacket packet) {
    Ping ping = (Ping) packet.data.data(new Ping());
    FlatBufferBuilder builder = new FlatBufferBuilder(0);
    int dataOffset = Ping.createPing(builder, ping.tickCount(), ping.sendTime(), TimeUtils.millis() - packet.time, false);
    int root = D2GS.createD2GS(builder, D2GSData.Ping, dataOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, root);
    D2GSPacket response = D2GSPacket.obtain(1 << packet.id, D2GSData.Ping, builder.dataBuffer());
    outPackets.offer(response);
  }

  private void notifyChannelInactive(ChannelHandlerContext ctx) {
    Gdx.app.debug(TAG, "notifyChannelInactive");
    SocketAddress from = endpoint.getSender(ctx, null);
    disconnect(ctx, from);
  }

  @ChannelHandler.Sharable
  private static class ConnectionLimiter extends ChannelInboundHandlerAdapter {
    static final String TAG = "ConnectionLimiter";

    final int maxClients;
    final AtomicInteger connections = new AtomicInteger();

    ConnectionLimiter(int maxClients) {
      this.maxClients = maxClients;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      int count = connections.incrementAndGet();
      if (count <= maxClients) {
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
      Gdx.app.debug(TAG, String.format("connection closed. %d / %d", count, maxClients));
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
