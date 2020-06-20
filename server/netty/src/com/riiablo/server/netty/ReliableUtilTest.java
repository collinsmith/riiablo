package com.riiablo.server.netty;

import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;

import com.riiablo.net.packet.netty.Connection;
import com.riiablo.net.packet.netty.Netty;
import com.riiablo.net.packet.netty.NettyData;

public class ReliableUtilTest {
  public static void main(String[] args) {
    ByteBuf bb = null;
    try {
      bb = Unpooled.buffer();
      test(bb);
    } finally {
      ReferenceCountUtil.release(bb);
    }
  }

  static void test(ByteBuf bb) {
    final int PROTOCOL = 0b10010001;
    final int SEQ      = 0xF0F0;
    final int ACK      = 0x0F0F;
    final int ACK_BITS = 0xFF0000FF;

    ReliableUtil.setProtocol(bb, PROTOCOL);
    ReliableUtil.setSEQ(bb, SEQ);
    ReliableUtil.setACK(bb, ACK);
    ReliableUtil.setACK_BITS(bb, ACK_BITS);

    bb.writerIndex(ReliableUtil.CONTENT_OFFSET); // hack to force writer position passed header
    System.out.println(ByteBufUtil.hexDump(bb)); // note: hexDump requires writerIndex

    FlatBufferBuilder builder = new FlatBufferBuilder();
    Connection.startConnection(builder);
    int dataOffset = Connection.endConnection(builder);
    int offset = Netty.createNetty(builder, PROTOCOL, SEQ, ACK, ACK_BITS, NettyData.Connection, dataOffset);
    Netty.finishNettyBuffer(builder, offset);
    ReliableUtil.setContent(bb, builder.dataBuffer());

    bb.writerIndex(ReliableUtil.CONTENT_OFFSET + ReliableUtil.getContentSize(bb)); // hack to force writer position passed content
    System.out.println(ByteBufUtil.hexDump(bb)); // note: hexDump requires writerIndex

    System.out.printf("%-8s %-5s %02x%n", "PROTOCOL", PROTOCOL == ReliableUtil.getProtocol(bb), ReliableUtil.getProtocol(bb));
    System.out.printf("%-8s %-5s %04x%n", "SEQ", SEQ == ReliableUtil.getSEQ(bb), ReliableUtil.getSEQ(bb));
    System.out.printf("%-8s %-5s %04x%n", "ACK", ACK == ReliableUtil.getACK(bb), ReliableUtil.getACK(bb));
    System.out.printf("%-8s %-5s %08x%n", "ACK_BITS", ACK_BITS == ReliableUtil.getACK_BITS(bb), ReliableUtil.getACK_BITS(bb));
    System.out.printf("%-8s %-5s %04x%n", "CSIZE", builder.dataBuffer().remaining() == ReliableUtil.getContentSize(bb), ReliableUtil.getContentSize(bb));
    System.out.printf("%-8s %-5s %s%n",   "CONTENT", StringUtils.equals(ByteBufUtil.hexDump(builder.sizedByteArray()), ByteBufUtil.hexDump(ReliableUtil.getContent(bb))), ByteBufUtil.hexDump(ReliableUtil.getContent(bb)));
  }
}
