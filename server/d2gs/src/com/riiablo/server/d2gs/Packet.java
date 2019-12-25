package com.riiablo.server.d2gs;

import com.google.flatbuffers.ByteBufferUtil;

import com.riiablo.net.packet.d2gs.D2GS;

import java.nio.ByteBuffer;

public class Packet {
    public int id;
    public ByteBuffer buffer;
    public D2GS data;

    public static Packet obtain(int id, ByteBuffer buffer) {
      Packet packet = new Packet();
      packet.id = id;
      packet.buffer = buffer;
      packet.data = D2GS.getRootAsD2GS(ByteBufferUtil.removeSizePrefix(buffer));
      return packet;
    }
  }
