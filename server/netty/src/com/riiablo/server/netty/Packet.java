package com.riiablo.server.netty;

import com.google.flatbuffers.ByteBufferUtil;
import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.net.packet.netty.Netty;

public class Packet {
  public int        id;
  public long       time;
  public ByteBuffer buffer;
  public Netty data;

  public static Packet obtain(int id, ByteBuffer buffer) {
    Packet packet = new Packet();
    packet.id = id;
    packet.time = TimeUtils.millis();
    packet.buffer = buffer;
    packet.data = Netty.getRootAsNetty(ByteBufferUtil.removeSizePrefix(buffer));
    return packet;
  }
}
