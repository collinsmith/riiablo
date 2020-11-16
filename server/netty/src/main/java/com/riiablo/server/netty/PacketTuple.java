package com.riiablo.server.netty;

import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.net.packet.netty.Netty;

public class PacketTuple {
  public int        id;
  public long       time;
  public ByteBuffer buffer;
  public Netty      data;

  public static PacketTuple obtain(int id, ByteBuffer buffer) {
    PacketTuple packet = new PacketTuple();
    packet.id = id;
    packet.time = TimeUtils.millis();
    packet.buffer = buffer;
    packet.data = Netty.getRootAsNetty(buffer);
    return packet;
  }
}
