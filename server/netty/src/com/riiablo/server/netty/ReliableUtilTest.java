package com.riiablo.server.netty;

import com.google.flatbuffers.FlatBufferBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.StringUtils;

import com.riiablo.net.packet.netty.Connection;
import com.riiablo.net.packet.netty.Netty;
import com.riiablo.net.packet.netty.NettyData;
import com.riiablo.util.BufferUtils;

public class ReliableUtilTest {
  public static void main(String[] args) {
    ByteBuf bb = null;
    try {
      bb = Unpooled.buffer();
      test(bb);
    } finally {
      ReferenceCountUtil.release(bb);
    }

    System.out.println("----");

    CompositeByteBuf composite = null;
    try {
      composite = Unpooled.compositeBuffer(2);
      testComposite(composite);
    } finally {
      ReferenceCountUtil.release(composite);
    }
  }

  static void test(ByteBuf bb) {
    final int PROTOCOL = 0b10010001;
    final int SEQ      = 0xF0F0;
    final int ACK      = 0x0F0F;
    final int ACK_BITS = 0xFF0000FF;

    Packet.Single.setProtocol(bb, PROTOCOL);
    Packet.Single.setSEQ(bb, SEQ);
    Packet.Single.setACK(bb, ACK);
    Packet.Single.setACK_BITS(bb, ACK_BITS);

    bb.writerIndex(Packet.Single.CONTENT_OFFSET); // hack to force writer position passed header
    System.out.println(ByteBufUtil.hexDump(bb)); // note: hexDump requires writerIndex

    FlatBufferBuilder builder = new FlatBufferBuilder();
    Connection.startConnection(builder);
    int dataOffset = Connection.endConnection(builder);
    int offset = Netty.createNetty(builder, 0L, NettyData.Connection, dataOffset);
    Netty.finishNettyBuffer(builder, offset);
    Packet.Single.setContent(bb, builder.dataBuffer());

    bb.writerIndex(Packet.Single.CONTENT_OFFSET + Packet.Single.getContentSize(bb)); // hack to force writer position passed content
    System.out.println(ByteBufUtil.hexDump(bb)); // note: hexDump requires writerIndex

    System.out.printf("%-8s %-5s %02x%n", "PROTOCOL", PROTOCOL == Packet.Single.getProtocol(bb), Packet.Single.getProtocol(bb));
    System.out.printf("%-8s %-5s %04x%n", "SEQ", SEQ == Packet.Single.getSEQ(bb), Packet.Single.getSEQ(bb));
    System.out.printf("%-8s %-5s %04x%n", "ACK", ACK == Packet.Single.getACK(bb), Packet.Single.getACK(bb));
    System.out.printf("%-8s %-5s %08x%n", "ACK_BITS", ACK_BITS == Packet.Single.getACK_BITS(bb), Packet.Single.getACK_BITS(bb));
    System.out.printf("%-8s %-5s %04x%n", "CSIZE", builder.dataBuffer().remaining() == Packet.Single.getContentSize(bb), Packet.Single.getContentSize(bb));
    System.out.printf("%-8s %-5s %s%n",   "CONTENT", StringUtils.equals(ByteBufUtil.hexDump(builder.sizedByteArray()), ByteBufUtil.hexDump(Packet.Single.getContent(bb))), ByteBufUtil.hexDump(Packet.Single.getContent(bb)));
  }

  static void testComposite(CompositeByteBuf bb) {
    final int PROTOCOL = 0b10010001;
    final int SEQ      = 0xF0F0;
    final int ACK      = 0x0F0F;
    final int ACK_BITS = 0xFF0000FF;

    ByteBuf bbHeader = bb.alloc().buffer();
    Packet.Single.createHeader(bbHeader, PROTOCOL, SEQ, ACK, ACK_BITS);
    System.out.println("HEADER:  " + ByteBufUtil.hexDump(bbHeader)); // note: hexDump requires writerIndex

    FlatBufferBuilder builder = new FlatBufferBuilder();
    Connection.startConnection(builder);
    int dataOffset = Connection.endConnection(builder);
    int offset = Netty.createNetty(builder, 0L, NettyData.Connection, dataOffset);
    Netty.finishNettyBuffer(builder, offset);

    ByteBuf bbContent = bb.alloc().buffer();
    ByteBuffer dataBuffer = builder.dataBuffer();
    dataBuffer.mark();
    bbContent.writeBytes(builder.dataBuffer());
    dataBuffer.reset();

    System.out.println("CONTENT: " + ByteBufUtil.hexDump(bbContent)); // note: hexDump requires writerIndex

    bb.addComponents(true, bbHeader, bbContent);

    Packet.Single.setContentSize(bb, bbContent.readableBytes());
    System.out.println(ByteBufUtil.hexDump(bb)); // note: hexDump requires writerIndex

    System.out.printf("%-8s %-5s %02x%n", "PROTOCOL", PROTOCOL == Packet.Single.getProtocol(bb), Packet.Single.getProtocol(bb));
    System.out.printf("%-8s %-5s %04x%n", "SEQ", SEQ == Packet.Single.getSEQ(bb), Packet.Single.getSEQ(bb));
    System.out.printf("%-8s %-5s %04x%n", "ACK", ACK == Packet.Single.getACK(bb), Packet.Single.getACK(bb));
    System.out.printf("%-8s %-5s %08x%n", "ACK_BITS", ACK_BITS == Packet.Single.getACK_BITS(bb), Packet.Single.getACK_BITS(bb));
    System.out.printf("%-8s %-5s %04x%n", "CSIZE", builder.dataBuffer().remaining() == Packet.Single.getContentSize(bb), Packet.Single.getContentSize(bb));
    System.out.printf("%-8s %-5s %s%n",   "CONTENT", StringUtils.equals(ByteBufUtil.hexDump(builder.sizedByteArray()), ByteBufUtil.hexDump(Packet.Single.getContent(bb))), ByteBufUtil.hexDump(Packet.Single.getContent(bb)));

    ByteBuf content = Packet.Single.getContent(bb);
    ByteBuffer nioContent = content.nioBuffer();
    System.out.println(ByteBufUtil.hexDump(BufferUtils.readRemaining(nioContent)));
  }
}
