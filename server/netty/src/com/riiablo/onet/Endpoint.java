package com.riiablo.onet;

import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;

public interface Endpoint<T> extends PacketSender<Object> {
  void reset();
  void update(float delta);
  void messageReceived(ChannelHandlerContext ctx, SocketAddress from, T msg);
  SocketAddress getRemoteAddress(ChannelHandlerContext ctx, T msg);
}
