package com.riiablo.onet;

import java.nio.ByteBuffer;

public interface UnicastEndpoint<T> extends Endpoint<T> {
  void sendMessage(ByteBuffer bb);
  void sendMessage(Object qos, ByteBuffer bb);
}
