package com.riiablo.net.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;

import com.riiablo.net.Endpoint;
import com.riiablo.net.PacketProcessor;

public class TcpEndpoint implements Endpoint<ByteBuf> {
  private static final String TAG = "TcpEndpoint";

  private static final boolean DEBUG = true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  private final Channel channel;
  private final PacketProcessor packetProcessor;

  public TcpEndpoint(Channel channel, PacketProcessor packetProcessor) {
    this.channel = channel;
    this.packetProcessor = packetProcessor;
  }

  @Override
  public Channel channel() {
    return channel;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) {
    if (DEBUG_RECEIVE) Gdx.app.debug(TAG, "onMessageReceived");
    packetProcessor.processPacket(ctx, msg);
  }

  @Override
  public void sendMessage(InetSocketAddress to, ByteBuffer bb) {
    if (DEBUG_SEND) Gdx.app.debug(TAG, "sendMessage");
    assert to == channel.remoteAddress();
    channel.writeAndFlush(Unpooled.wrappedBuffer(bb)); // releases msg
  }

  @Override
  public void sendMessage(InetSocketAddress to, Object qos, ByteBuffer bb) {
    sendMessage(to, bb);
  }

  @Override
  public void reset() {}

  @Override
  public void update(float delta) {}
}
