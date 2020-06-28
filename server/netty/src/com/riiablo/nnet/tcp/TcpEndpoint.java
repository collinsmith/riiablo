package com.riiablo.nnet.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;

import com.riiablo.nnet.Endpoint;
import com.riiablo.nnet.PacketProcessor;

public class TcpEndpoint implements Endpoint<ByteBuf> {
  private static final String TAG = "TcpEndpoint";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  private final IdResolver<Channel> resolver;
  private final PacketProcessor packetProcessor;

  public TcpEndpoint(IdResolver<Channel> resolver, PacketProcessor packetProcessor) {
    this.resolver = resolver;
    this.packetProcessor = packetProcessor;
  }

  @Override
  public SocketAddress getSender(ChannelHandlerContext ctx, ByteBuf msg) {
    return ctx.channel().remoteAddress();
  }

  @Override
  public void sendMessage(int id, ByteBuffer bb, int qos) {
    if (DEBUG_SEND) Gdx.app.debug(TAG, "sendMessage to " + id);
    Channel ch = resolver.get(id);
    ch.writeAndFlush(Unpooled.wrappedBuffer(bb));
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, SocketAddress from, ByteBuf msg) {
    if (DEBUG_RECEIVE) Gdx.app.debug(TAG, "onMessageReceived from " + from);
    packetProcessor.processPacket(ctx, from, msg);
  }

  /*
  private final ConcurrentHashMap<SocketAddress, Channel> selector = new ConcurrentHashMap<>(32);
//  private final IntObjectMap<Channel> ids = new IntObjectHashMap<>(32);
  private final ObjectIntMap<SocketAddress> ids = new ObjectIntMap<>(32);

  private final PacketProcessor packetProcessor;

  public TcpEndpoint(PacketProcessor packetProcessor) {
    this.packetProcessor = packetProcessor;
  }

  @Override
  public void clear() {
    selector.clear();
  }

  @Override
  public Channel register(SocketAddress to, Channel ch) {
    return selector.put(to, ch);
  }

  @Override
  public Channel delete(SocketAddress to) {
    return selector.remove(to);
  }

  @Override
  public SocketAddress getSender(ChannelHandlerContext ctx, ByteBuf msg) {
    return ctx.channel().remoteAddress();
  }

  @Override
  public void sendMessage(int id, Object qos, ByteBuffer bb) {
    if (DEBUG_SEND) Gdx.app.debug(TAG, "sendMessage to " + id);
    selector.get(id).writeAndFlush(Unpooled.wrappedBuffer(bb));
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, SocketAddress from, ByteBuf msg) {
    if (DEBUG_RECEIVE) Gdx.app.debug(TAG, "onMessageReceived from " + from);
    packetProcessor.processPacket(ctx, from, msg);
  }

  @Override
  public Channel register(int id, Channel to) {
    return ids.put(id, to);
  }

  @Override
  public Channel delete(int id) {
    return ids.remove(id);
  }
  */
}
