package com.riiablo.net.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.Gdx;

import com.riiablo.net.Endpoint;
import com.riiablo.net.IntResolver;
import com.riiablo.net.MessageProcessor;

public class TcpEndpoint implements Endpoint<ByteBuf> {
  private static final String TAG = "TcpEndpoint";

  private static final boolean DEBUG = !true;
  private static final boolean DEBUG_SEND = DEBUG && true;
  private static final boolean DEBUG_RECEIVE = DEBUG && true;

  private final IntResolver<Channel> channels;
  private final MessageProcessor processor;

  public TcpEndpoint(IntResolver<Channel> channelResolver, MessageProcessor messageProcessor) {
    this.channels = channelResolver;
    this.processor = messageProcessor;
  }

  @Override
  public SocketAddress getSender(ChannelHandlerContext ctx, ByteBuf msg) {
    return ctx.channel().remoteAddress();
  }

  @Override
  public void sendMessage(int id, ByteBuffer buffer, int qos) {
    if (DEBUG_SEND) Gdx.app.debug(TAG, "sendMessage to " + id);
    channels.get(id).writeAndFlush(Unpooled.wrappedBuffer(buffer));
  }

  @Override
  public void processMessage(ChannelHandlerContext ctx, SocketAddress from, ByteBuf msg) {
    if (DEBUG_RECEIVE) Gdx.app.debug(TAG, "processMessage from " + from);
    processor.processMessage(ctx, from, msg);
  }
}
