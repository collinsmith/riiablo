package com.riiablo.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;

public interface MessageProcessor {
  void processMessage(ChannelHandlerContext ctx, SocketAddress from, ByteBuf msg);
}
