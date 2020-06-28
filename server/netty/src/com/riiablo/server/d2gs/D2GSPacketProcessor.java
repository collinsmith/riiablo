package com.riiablo.server.d2gs;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;

import com.badlogic.gdx.Gdx;

import com.riiablo.net.packet.netty.Netty;
import com.riiablo.net.packet.netty.NettyData;
import com.riiablo.nnet.PacketProcessor;

public class D2GSPacketProcessor implements PacketProcessor {
  private static final String TAG = "D2GSPacketProcessor";

  @Override
  public void processPacket(ChannelHandlerContext ctx, SocketAddress from, ByteBuf bb) {
    Gdx.app.debug(TAG, "Processing packet from " + from + "...");
    Gdx.app.debug(TAG, "  " + ByteBufUtil.hexDump(bb));
    processPacket(ctx, from, Netty.getRootAsNetty(bb.nioBuffer()));
  }

  public void processPacket(ChannelHandlerContext ctx, SocketAddress from, Netty netty) {
    Gdx.app.debug(TAG, "  " + "dataType=" + NettyData.name(netty.dataType()));
    switch (netty.dataType()) {
      default:
        Gdx.app.debug(TAG, "unknown data type: " + netty.dataType());
        Gdx.app.debug(TAG, "  " + "closing " + ctx);
        ctx.close();
    }
  }
}
