package com.riiablo.net;

import java.nio.ByteBuffer;

public interface UnicastEndpoint<T> extends Endpoint<T> {
  void sendMessage(ByteBuffer bb);
  void sendMessage(Object qos, ByteBuffer bb);
}
