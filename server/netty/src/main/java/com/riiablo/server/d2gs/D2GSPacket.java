package com.riiablo.server.d2gs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.badlogic.gdx.utils.TimeUtils;

import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;
import com.riiablo.nnet.Packet;

// TODO: separate to the different use-cases -- incoming requires context, outgoing doesn't
public class D2GSPacket extends Packet<D2GS> {
  ChannelHandlerContext ctx;
  SocketAddress from;
  ByteBuf bb;
  byte dataType;

  @Override
  public String toString() {
    return D2GSData.name(dataType);
  }

  // incoming
  public static D2GSPacket obtain(ChannelHandlerContext ctx, SocketAddress from, ByteBuf bb) {
    D2GSPacket packet = new D2GSPacket();
    packet.id = Server.INVALID_CLIENT;
    packet.time = TimeUtils.millis();

    packet.ctx = ctx;
    packet.from = from;
    packet.bb = bb;

    packet.buffer = bb.nioBuffer();
    packet.data = D2GS.getRootAsD2GS(packet.buffer);
    packet.dataType = packet.data.dataType();
    return packet;
  }

  // outgoing
  public static D2GSPacket obtain(int id, byte dataType, ByteBuffer bb) {
    D2GSPacket packet = new D2GSPacket();
    packet.id = id;
    packet.time = TimeUtils.millis();

    packet.ctx = null;
    packet.from = null;
    packet.bb = null;

    packet.buffer = bb;
    packet.data = null;
    packet.dataType = dataType;
    return packet;
  }
}
