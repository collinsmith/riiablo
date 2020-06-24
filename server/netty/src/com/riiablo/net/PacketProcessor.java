package com.riiablo.net;

import io.netty.buffer.ByteBuf;

public interface PacketProcessor {
  void processPacket(ByteBuf bb);
}
