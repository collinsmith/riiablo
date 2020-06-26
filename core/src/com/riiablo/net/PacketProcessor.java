package com.riiablo.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface PacketProcessor {
  void processPacket(ChannelHandlerContext ctx, ByteBuf bb);
}
