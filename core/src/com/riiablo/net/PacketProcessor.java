package com.riiablo.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;

public interface PacketProcessor {
  void processPacket(ChannelHandlerContext ctx, SocketAddress from, ByteBuf bb);
}
