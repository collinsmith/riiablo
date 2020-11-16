package com.riiablo.nnet;

import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface Endpoint<T> {
  void sendMessage(int id, ByteBuffer bb, int qos);
  void messageReceived(ChannelHandlerContext ctx, SocketAddress from, T msg);
  SocketAddress getSender(ChannelHandlerContext ctx, T msg);

  interface IdResolver<R> {
    R get(int id);
  }
}
