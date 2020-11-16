package com.riiablo.net.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;
import java.nio.ByteBuffer;

public class OutboundPacket {
  public static final int BROADCAST = 0xFFFFFFFF;

  public static OutboundPacket obtain(int id, byte dataType, ByteBuffer buffer) {
    return new OutboundPacket(id, dataType, buffer);
  }

// TODO: Implement support for efficient retaining of existing packet data
//  public static OutboundPacket echo(InboundPacket packet) {}

  private final int id;
  private final byte dataType;
  private final ByteBuffer buffer;

  private ByteBuf data;

  OutboundPacket(int id, byte dataType, ByteBuffer buffer) {
    this.id = id;
    this.dataType = dataType;
    this.buffer = buffer;
  }

  public int id() {
    return id;
  }

  public byte dataType() {
    return dataType;
  }

  public String dataTypeName() {
    return null;
  }

  public ByteBuffer buffer() {
    return buffer;
  }

  public ByteBuf content() {
    if (data == null) data = Unpooled.wrappedBuffer(buffer);
    if (data.refCnt() <= 0) throw new IllegalReferenceCountException(data.refCnt());
    return data;
  }

  @Override
  public String toString() {
    return String.format("%s (0x%02x) to %08X", dataTypeName(), dataType() & 0xFF, id());
  }
}
