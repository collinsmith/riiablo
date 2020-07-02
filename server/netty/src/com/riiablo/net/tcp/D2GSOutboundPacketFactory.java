package com.riiablo.net.tcp;

import java.nio.ByteBuffer;

import com.riiablo.net.packet.d2gs.D2GSData;

public class D2GSOutboundPacketFactory {
  public static D2GSOutboundPacket obtain(int id, byte dataType, ByteBuffer buffer) {
    return new D2GSOutboundPacket(id, dataType, buffer);
  }

  static class D2GSOutboundPacket extends OutboundPacket {
    D2GSOutboundPacket(int id, byte dataType, ByteBuffer buffer) {
      super(id, dataType, buffer);
    }

    @Override
    public String dataTypeName() {
      return D2GSData.name(dataType());
    }
  }
}
