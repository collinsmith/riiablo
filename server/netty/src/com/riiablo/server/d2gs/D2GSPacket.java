package com.riiablo.server.d2gs;

import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.TimeUtils;

public class D2GSPacket<T extends Table> {
  public int id;
  public long time;
  public ByteBuffer buffer;
  public T data;

  public static <T extends Table> D2GSPacket<T> obtain(int id, T fb) {
    D2GSPacket<T> packet = new D2GSPacket<>();
    packet.id = id;
    packet.time = TimeUtils.millis();
    packet.buffer = fb.getByteBuffer();
    packet.data = fb;
    return packet;
  }
}
