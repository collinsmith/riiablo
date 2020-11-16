package com.riiablo.nnet;

import com.google.flatbuffers.Table;
import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.TimeUtils;

public class Packet<T extends Table> {
  public int id;
  public long time;
  public ByteBuffer buffer;
  public T data;

  public static <T extends Table> Packet<T> obtain(int id, T fb) {
    Packet<T> packet = new Packet<>();
    packet.id = id;
    packet.time = TimeUtils.millis();
    packet.buffer = fb.getByteBuffer();
    packet.data = fb;
    return packet;
  }
}
