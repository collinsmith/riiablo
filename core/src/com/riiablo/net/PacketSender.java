package com.riiablo.net;

import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface PacketSender<T> {
  Channel channel();
  void sendMessage(InetSocketAddress to, ByteBuffer bb);
  void sendMessage(InetSocketAddress to, T qos, ByteBuffer bb);
}
