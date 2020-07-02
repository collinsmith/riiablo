package com.riiablo.net;

import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface Endpoint<T> {
  void sendMessage(int id, ByteBuffer buffer, int qos);
  void processMessage(ChannelHandlerContext ctx, SocketAddress from, T msg);
  SocketAddress getSender(ChannelHandlerContext ctx, T msg);
}
