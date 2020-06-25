package com.riiablo.net;

import io.netty.channel.Channel;
import java.nio.ByteBuffer;

public interface PacketSender<T> {
  Channel channel();
  void sendMessage(ByteBuffer bb);
  void sendMessage(T qos, ByteBuffer bb);
}
