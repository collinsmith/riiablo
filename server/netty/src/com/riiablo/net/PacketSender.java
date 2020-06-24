package com.riiablo.net;

import java.nio.ByteBuffer;

public interface PacketSender<T> {
  void sendMessage(ByteBuffer bb);
  void sendMessage(T qos, ByteBuffer bb);
}
