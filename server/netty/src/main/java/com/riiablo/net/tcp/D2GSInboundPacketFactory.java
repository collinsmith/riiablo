package com.riiablo.net.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;

import com.riiablo.net.packet.d2gs.D2GS;
import com.riiablo.net.packet.d2gs.D2GSData;

public final class D2GSInboundPacketFactory {
  public static D2GSInboundPacket obtain(ChannelHandlerContext ctx, InetSocketAddress sender, ByteBuf message) {
    return new D2GSInboundPacket(ctx, sender, message);
  }

  static class D2GSInboundPacket extends InboundPacket<D2GS> {
    D2GSInboundPacket(ChannelHandlerContext ctx, InetSocketAddress sender, ByteBuf message) {
      super(D2GS.class, ctx, sender, message);
      table = D2GS.getRootAsD2GS(buffer);
    }

    @Override
    public byte dataType() {
      return table.dataType();
    }

    @Override
    public String dataTypeName() {
      return D2GSData.name(dataType());
    }

    @Override
    public D2GSInboundPacket replace(ByteBuf content) {
      return obtain(ctx(), sender(), content);
    }
  }
}
