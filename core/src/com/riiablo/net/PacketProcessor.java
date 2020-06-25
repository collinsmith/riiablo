package com.riiablo.net;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface PacketProcessor {
  void processPacket(Channel ch, ByteBuf bb);
}
