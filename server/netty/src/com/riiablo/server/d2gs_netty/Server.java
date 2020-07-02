package com.riiablo.server.d2gs_netty;

import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
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
import java.net.InetSocketAddress;
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

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import com.artemis.utils.BitVector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.Riiablo;
import com.riiablo.engine.Engine;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.server.component.Player;
import com.riiablo.map.Map;
import com.riiablo.net.Endpoint;
import com.riiablo.net.InboundChannelHandler;
import com.riiablo.net.IntResolver;
import com.riiablo.net.MessageProcessor;
import com.riiablo.net.OutboundChannelHandler;
import com.riiablo.net.packet.d2gs.Connection;
import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.net.packet.d2gs.Disconnect;
import com.riiablo.net.packet.d2gs.Ping;
import com.riiablo.net.tcp.D2GSInboundPacketFactory;
import com.riiablo.net.tcp.D2GSOutboundPacketFactory;
import com.riiablo.net.tcp.InboundPacket;
import com.riiablo.net.tcp.OutboundPacket;
import com.riiablo.net.tcp.TcpEndpoint;
import com.riiablo.save.CharData;
import com.riiablo.util.DebugUtils;

public class Server implements MessageProcessor {
  private static final String TAG = "D2GS";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_EVENTS = DEBUG && true;
  private static final boolean DEBUG_CHILD_EVENTS = DEBUG_EVENTS && true;
  private static final boolean DEBUG_BINDING = DEBUG && true;
  private static final boolean DEBUG_INACTIVE = DEBUG && true;
  private static final boolean DEBUG_PROPAGATION = DEBUG && true;
  private static final boolean DEBUG_MSG_CONTENTS = DEBUG && true;
  private static final boolean DEBUG_CONNECTION = DEBUG && true;
  private static final boolean DEBUG_RECEIVED_CACHE = DEBUG && true;
  private static final boolean DEBUG_RECEIVED_PACKETS = DEBUG && true;
  private static final boolean DEBUG_SENT_PACKETS = DEBUG && true;
  private static final boolean DEBUG_SENT_CACHE = DEBUG && true;

  static final int MAX_CLIENTS = Riiablo.MAX_PLAYERS;

  private final InetAddress address;
  private final int port;

  private ChannelFuture future;
  private ServerBootstrap bootstrap;
  private EventLoopGroup parentGroup;
  private EventLoopGroup childGroup;

  private Endpoint<?> endpoint;
  private IntResolver<?> channels;

  private final ChannelInboundHandler connectionLimiter = new ConnectionLimiter(MAX_CLIENTS);
  private final ChannelInboundHandler connectionListener = new ConnectionListener();

  private final BlockingQueue<InboundPacket<D2GS>> inPackets = new ArrayBlockingQueue<>(32);
  private final Collection<InboundPacket<D2GS>> inCache = new ArrayList<>(32);
  final BlockingQueue<OutboundPacket> outPackets = new ArrayBlockingQueue<>(MAX_CLIENTS * 32);
  private final Collection<OutboundPacket> outCache = new ArrayList<>(MAX_CLIENTS * 32);

  static final BitVector ignoredPackets = new BitVector(D2GSData.names.length); {
    ignoredPackets.set(D2GSData.EntitySync);
  }

  private int connectedFlags;
  private final ObjectIntMap<SocketAddress> cdata = new ObjectIntMap<>(32);
  private final ClientData[] clients = new ClientData[MAX_CLIENTS]; {
    for (int i = 0; i < MAX_CLIENTS; i++) clients[i] = new ClientData();
    channels = new IntResolver<Channel>() {
      @Override
      public Channel get(int id) {
        return clients[id].channel;
      }
    };
  }

  final IntIntMap player = new IntIntMap();

  protected ComponentMapper<Player> mPlayer;

  @Wire(name = "messageProcessor")
  protected D2GSMessageProcessor messageProcessor;

  @Wire(name = "factory")
  protected EntityFactory factory;

  @Wire(name = "map")
  protected Map map;

  private final Main main; // FIXME: replace when Server is a PassiveSystem

  public Server(Main main, InetAddress address, int port) {
    this.main = main;
    this.address = address;
    this.port = port;
  }

  public ServerBootstrapConfig config() {
    return bootstrap.config();
  }

  public IntResolver resolver() {
    return channels;
  }

  @SuppressWarnings("unchecked")
  private static Endpoint<?> createEndpoint(IntResolver<?> channelResolver, MessageProcessor messageProcessor) {
    return new TcpEndpoint((IntResolver<Channel>) channelResolver, messageProcessor);
  }

  @SuppressWarnings("unchecked")
  private static <T> InboundChannelHandler<T> createInboundChannelHandler(Class<T> packetType, Endpoint<?> endpoint) {
    return new InboundChannelHandler<>(packetType, (Endpoint<T>) endpoint);
  }

  private static OutboundChannelHandler createOutboundChannelHandler() {
    return new OutboundChannelHandler();
  }

  public void create() {
    endpoint = createEndpoint(channels, this);
    parentGroup = new NioEventLoopGroup();
    childGroup = new NioEventLoopGroup();
    bootstrap = new ServerBootstrap()
        .group(parentGroup, childGroup)
        .channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG, 64)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel ch) {
            Gdx.app.debug(TAG, "initChannel " + ch);
            ch.pipeline()
                .addFirst(connectionLimiter)
                .addLast(connectionListener)
                .addLast(new SizePrefixedDecoder())
                .addLast(createInboundChannelHandler(ByteBuf.class, endpoint))
                .addLast(createOutboundChannelHandler())
                ;
          }
        })
        .childOption(ChannelOption.TCP_NODELAY, true)
        .localAddress(address, port)
        ;
  }

  public ChannelFuture start() {
    if (DEBUG_EVENTS) Gdx.app.log(TAG, "Starting server...");
    if (DEBUG_BINDING) Gdx.app.debug(TAG, "Attempting to bind to " + bootstrap.config().localAddress());
    future = bootstrap.bind(port);
    if (DEBUG_BINDING) {
      future.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          Gdx.app.debug(TAG, "Successfully bound to " + future.channel().localAddress());
        }
      });
    }
    return future;
  }

  public void dispose() {
    Gdx.app.log(TAG, "Notifying clients...");
    for (int id = 0; id < MAX_CLIENTS; id++) if (clients[id].connected) disconnect(id, 0);

    try {
      Gdx.app.log(TAG, "Shutting down channel...");
      future.channel().close();
      future.channel().closeFuture().syncUninterruptibly();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    try {
      Gdx.app.log(TAG, "Shutting down children...");
      childGroup.shutdownGracefully().syncUninterruptibly();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }

    try {
      Gdx.app.log(TAG, "Shutting down parent...");
      parentGroup.shutdownGracefully().syncUninterruptibly();
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
    }
  }

  public void updateIncoming(float delta) {
    assert inCache.isEmpty();
    int numCached = inPackets.drainTo(inCache);
    if (DEBUG_RECEIVED_CACHE && numCached > 0) Gdx.app.debug(TAG, "Processing " + numCached + " packets...");
    for (InboundPacket<D2GS> packet : inCache) {
      if (DEBUG_RECEIVED_PACKETS && !ignoredPackets.get(packet.dataType())) Gdx.app.debug(TAG, "Processing " + packet);
      try {
        packet.setId(cdata.get(packet.sender(), InboundPacket.INVALID_CLIENT));
        if (packet.id() == InboundPacket.INVALID_CLIENT && packet.dataType() != D2GSData.Connection) {
          Gdx.app.error(TAG, "  " + packet + " from invalid client and not a connection request");
          continue;
        }
        processPacket(packet);
      } finally {
        ReferenceCountUtil.release(packet);
        assert ReferenceCountUtil.refCnt(packet) == 0 : "refCnt: " + ReferenceCountUtil.refCnt(packet);
      }
    }
    inCache.clear();
  }

  public void updateOutgoing(float delta) {
    assert outCache.isEmpty();
    int numCached = outPackets.drainTo(outCache);
    if (DEBUG_SENT_CACHE && numCached > 0) Gdx.app.debug(TAG, "Sending " + numCached + " packets...");
    for (OutboundPacket packet : outCache) {
      if (DEBUG_SENT_PACKETS && !ignoredPackets.get(packet.dataType())) Gdx.app.debug(TAG, "Dispatching " + packet);
      for (int i = 0, flag = 1; i < MAX_CLIENTS; i++, flag <<= 1) {
        if ((packet.id() & flag) == flag && ((connectedFlags & flag) == flag || packet.dataType() == D2GSData.Connection)) {
          ClientData client = clients[i];
          if (!client.connected) continue;
          try {
            if (DEBUG_SENT_PACKETS && !ignoredPackets.get(packet.dataType())) Gdx.app.debug(TAG, "  " + "Dispatching packet to " + i);
            sendMessage(i, packet);
          } catch (Throwable t) {
            Gdx.app.error(TAG, t.getMessage(), t);
          }
        }
      }
    }
    outCache.clear();
  }

  private void sendMessage(int id, OutboundPacket packet) {
    packet.buffer().mark();
    endpoint.sendMessage(id, packet.buffer(), -1);
    packet.buffer().reset();
    if ((connectedFlags & (1 << id)) == 0 && packet.dataType() == D2GSData.Connection) {
      connectedFlags |= (1 << id);
    }
  }

  @Override
  public void processMessage(ChannelHandlerContext ctx, SocketAddress sender, ByteBuf msg) {
    if (DEBUG_RECEIVED_CACHE) Gdx.app.debug(TAG, "Queueing packet from " + sender);
    if (DEBUG_MSG_CONTENTS && DEBUG_RECEIVED_PACKETS) Gdx.app.debug(TAG, "  " + ByteBufUtil.hexDump(msg));
    InboundPacket<D2GS> packet = D2GSInboundPacketFactory.obtain(ctx, (InetSocketAddress) sender, msg);
    if (DEBUG_RECEIVED_CACHE) Gdx.app.debug(TAG, "  " + packet.toString("unknown"));
    // NOTE: packet sender id is not resolved until the message is processed
    boolean success = false;
    try {
      success = inPackets.offer(packet, 5, TimeUnit.MILLISECONDS); // TODO: what is a reasonable timeout
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
      success = false;
    } finally {
      if (success) {
        ReferenceCountUtil.retain(packet);
        assert ReferenceCountUtil.refCnt(packet) == 2 : "refCnt: " + ReferenceCountUtil.refCnt(packet);
      } else {
        Gdx.app.error(TAG, "Failed to add packet " + packet + " to inbound queue");
        disconnect(ctx, sender);
      }
    }
  }

  public void processPacket(InboundPacket<D2GS> packet) {
    if (DEBUG_PROPAGATION && !ignoredPackets.get(packet.dataType())) Gdx.app.debug(TAG, "  " + "processPacket " + packet);
    switch (packet.dataType()) {
      case D2GSData.Connection:
        onConnection(packet);
        break;
      case D2GSData.Disconnect:
        onDisconnect(packet);
        break;
      case D2GSData.Ping:
        onPing(packet);
        break;
      default:
        if (DEBUG_PROPAGATION && !ignoredPackets.get(packet.dataType())) Gdx.app.debug(TAG, "  " + "Propagating " + packet + " to " + messageProcessor);
        messageProcessor.processPacket(packet);
    }
  }

  private void onConnection(InboundPacket<D2GS> packet) {
    assert packet.dataType() == D2GSData.Connection;
    if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "  " + packet);

    synchronized (clients) {
      ClientData client = null;
      int id = InboundPacket.INVALID_CLIENT;
      final SocketAddress sender = packet.sender();
      final ClientData[] clients = this.clients;
      for (id = 0; id < MAX_CLIENTS && !sender.equals((client = clients[id]).address); id++);
      if (id == MAX_CLIENTS) {
        if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "  " + "No connection record found for " + sender);
        if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "  " + "Creating client data for " + sender);
        for (id = 0; id < MAX_CLIENTS && (client = clients[id]).connected; id++);
        if (id == MAX_CLIENTS) {
          Gdx.app.error(TAG, "  " + "Client connected, but no slot is available");
          disconnect(packet.ctx(), packet.sender());
          return;
        }

        if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "  " + String.format("Assigned %s to %d (0x%08x)", sender, id, 1 << id));
        client.connect(packet.ctx().channel(), packet.sender());
        packet.setId(id);
        cdata.put(sender, id);
      } else {
        if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "  " + String.format("Found connection record for %s as %d (0x%08x)", sender, id, 1 << id));
      }
    }

    Connection connection = (Connection) packet.table().data(new Connection());

    byte[] cofComponents = new byte[16];
    connection.cofComponentsAsByteBuffer().get(cofComponents);
    Gdx.app.debug(TAG, "  " + DebugUtils.toByteArray(cofComponents));

    byte[] cofAlphas = new byte[16];
    connection.cofAlphasAsByteBuffer().get(cofAlphas);
    Gdx.app.debug(TAG, "  " + Arrays.toString(cofAlphas));
    Gdx.app.debug(TAG, "  >" + Arrays.toString(com.riiablo.util.ArrayUtils.toFloatingPoint(cofAlphas)));

    byte[] cofTransforms = new byte[16];
    connection.cofTransformsAsByteBuffer().get(cofTransforms);
    Gdx.app.debug(TAG, "  " + DebugUtils.toByteArray(cofTransforms));

    ByteBuffer d2sData = connection.d2sAsByteBuffer();
    CharData charData = CharData.loadFromBuffer(main.diff, d2sData);
    Gdx.app.debug(TAG, "  " + charData);

    Gdx.app.log(TAG, String.format("Connection from %s : %s (Level %d %s)", packet.sender(), charData.name, charData.level, charData.classId));

    Vector2 origin = map.find(Map.ID.TOWN_ENTRY_1);
    if (origin == null) origin = map.find(Map.ID.TOWN_ENTRY_2);
    if (origin == null) origin = map.find(Map.ID.TP_LOCATION);
    int entityId = factory.createPlayer(charData, origin);
    player.put(packet.id(), entityId);
    Gdx.app.debug(TAG, "  entityId=" + entityId);

    FlatBufferBuilder builder = new FlatBufferBuilder();
    Connection.startConnection(builder);
    Connection.addEntityId(builder, entityId);
    int connectionOffset = Connection.endConnection(builder);
    int offset = D2GS.createD2GS(builder, D2GSData.Connection, connectionOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, offset);
    OutboundPacket response = D2GSOutboundPacketFactory.obtain(packet.flag(), D2GSData.Connection, builder.dataBuffer());
    outPackets.offer(response);

    Synchronize(packet.id(), entityId);

    BroadcastConnect(packet.id(), ~packet.flag(), connection, charData, entityId);
  }

  // TODO intention is to prepare a larger reliable sync packet with world state
  private void Synchronize(int id, int entityId) {}

  private void BroadcastConnect(int id, int flags, Connection connection, CharData charData, int entityId) {
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

    OutboundPacket broadcast = D2GSOutboundPacketFactory.obtain(flags, D2GSData.Connection, builder.dataBuffer());
    boolean success = outPackets.offer(broadcast);
    assert success;
  }

  private void onDisconnect(InboundPacket<D2GS> packet) {
    assert packet.dataType() == D2GSData.Disconnect;
    if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "  " + packet);

    Disconnect disconnect = (Disconnect) packet.table().data(new Disconnect());
    int entityId = player.get(packet.id(), Engine.INVALID_ENTITY);
    if (entityId != Engine.INVALID_ENTITY) {
      CharData charData = mPlayer.get(entityId).data;
      Gdx.app.log(TAG, String.format("Disconnection from %s : %s (Level %d %s)", packet.sender(), charData.name, charData.level, charData.classId));
    } else {
      Gdx.app.log(TAG, String.format("Disconnection from %s", packet.sender()));
    }
    disconnect(packet.ctx(), packet.sender());
  }

  private void notifyChannelInactive(ChannelHandlerContext ctx) {
    SocketAddress address = endpoint.getSender(ctx, null);
    disconnect(ctx, address);
  }

  private void disconnect(ChannelHandlerContext ctx, SocketAddress address) {
    if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "Disconnecting " + address);
    synchronized (clients) {
      ClientData client = null;
      int id = InboundPacket.INVALID_CLIENT;
      for (id = 0; id < MAX_CLIENTS && !address.equals((client = clients[id]).address); id++);
      if (id == MAX_CLIENTS) {
        if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "  " + String.format("Client from %s is already disconnected", address));
      } else {
        if (DEBUG_CONNECTION) Gdx.app.debug(TAG, "  " + String.format("Found connection record for %s as %d (0x%08x)", address, id, 1 << id));

        client.disconnect();
        connectedFlags &= ~(1 << id);
        cdata.remove(address, InboundPacket.INVALID_CLIENT);
        disconnect(id, ~(1 << id));
      }
    }

    if (DEBUG_CHILD_EVENTS) Gdx.app.debug(TAG, "  " + String.format("Closing %s", ctx));
    ctx.close();
  }

  private void disconnect(int id, int flags) {
    int entityId = player.get(id, Engine.INVALID_ENTITY);
    assert entityId != Engine.INVALID_ENTITY;

    if (DEBUG_CONNECTION) {
      CharData charData = mPlayer.get(entityId).data;
      Gdx.app.debug(TAG, "  " + String.format("Disconnecting %d (0x%08x) %s (Level %d %s)", id, 1 << id, charData.name, charData.level, charData.classId));
    }

    FlatBufferBuilder builder = new FlatBufferBuilder();
    int disconnectOffset = Disconnect.createDisconnect(builder, entityId);
    int offset = D2GS.createD2GS(builder, D2GSData.Disconnect, disconnectOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, offset);
    OutboundPacket broadcast = D2GSOutboundPacketFactory.obtain(flags, D2GSData.Disconnect, builder.dataBuffer());
    outPackets.offer(broadcast);

    main.world.delete(entityId);
    player.remove(id, Engine.INVALID_ENTITY);
  }

  private void onPing(InboundPacket<D2GS> packet) {
    Ping ping = (Ping) packet.table().data(new Ping());
    FlatBufferBuilder builder = new FlatBufferBuilder(0);
    int dataOffset = Ping.createPing(builder, ping.tickCount(), ping.sendTime(), TimeUtils.millis() - packet.time(), false);
    int root = D2GS.createD2GS(builder, D2GSData.Ping, dataOffset);
    D2GS.finishSizePrefixedD2GSBuffer(builder, root);
    OutboundPacket response = D2GSOutboundPacketFactory.obtain(packet.flag(), D2GSData.Ping, builder.dataBuffer());
    outPackets.offer(response);
  }

  @ChannelHandler.Sharable
  private class ConnectionListener extends ChannelInboundHandlerAdapter {
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      if (DEBUG_INACTIVE) Gdx.app.debug(TAG, ctx.channel() + " triggered inactive");
      super.channelInactive(ctx);
      notifyChannelInactive(ctx);
    }
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

  private static class ClientData {
    SocketAddress address;
    Channel channel;
    boolean connected;

    ClientData connect(Channel channel, SocketAddress address) {
      assert !connected;
      this.channel = channel;
      this.address = address;
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
      return connected ? String.format("[%s]", address) : "[disconnected]";
    }
  }
}
