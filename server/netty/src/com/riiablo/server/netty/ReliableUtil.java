package com.riiablo.server.netty;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

public class ReliableUtil {
  private ReliableUtil() {}

  private static final int PROTOCOL_OFFSET     = 0;
  private static final int PROTOCOL_SIZE       = 1; // ubyte

  private static final int SEQ_OFFSET          = PROTOCOL_OFFSET + PROTOCOL_SIZE;
  private static final int SEQ_SIZE            = 2; // ushort

  private static final int ACK_OFFSET          = SEQ_OFFSET + SEQ_SIZE;
  private static final int ACK_SIZE            = 2; // ushort

  private static final int ACK_BITS_OFFSET     = ACK_OFFSET + ACK_SIZE;
  private static final int ACK_BITS_SIZE       = 4; // int

  private static final int CONTENT_SIZE_OFFSET = ACK_BITS_OFFSET + ACK_BITS_SIZE;
  private static final int CONTENT_SIZE_SIZE   = 2; // ushort

  static final int CONTENT_OFFSET = CONTENT_SIZE_OFFSET + CONTENT_SIZE_SIZE;

  public static int getProtocol(ByteBuf bb) {
    return bb.getUnsignedByte(PROTOCOL_OFFSET);
  }

  public static int getSEQ(ByteBuf bb) {
    return bb.getUnsignedShort(SEQ_OFFSET);
  }

  public static int getACK(ByteBuf bb) {
    return bb.getUnsignedShort(ACK_OFFSET);
  }

  public static int getACK_BITS(ByteBuf bb) {
    return bb.getInt(ACK_BITS_OFFSET);
  }

  public static int getContentSize(ByteBuf bb) {
    return bb.getUnsignedShort(CONTENT_SIZE_OFFSET);
  }

  public static ByteBuf getContent(ByteBuf bb) {
    return bb.slice(CONTENT_OFFSET, getContentSize(bb));
  }

  static void setProtocol(ByteBuf bb, int value) {
    bb.setByte(PROTOCOL_OFFSET, value);
  }

  static void setSEQ(ByteBuf bb, int value) {
    bb.setShort(SEQ_OFFSET, value);
  }

  static void setACK(ByteBuf bb, int value) {
    bb.setShort(ACK_OFFSET, value);
  }

  static void setACK_BITS(ByteBuf bb, int value) {
    bb.setInt(ACK_BITS_OFFSET, value);
  }

  static void setContentSize(ByteBuf bb, int value) {
    Validate.isTrue(value <= 0xFFFF, "cannot encode content size as ushort, src.remaining()=" + value);
    bb.setShort(CONTENT_SIZE_OFFSET, value);
  }

  static void setContent(ByteBuf bb, ByteBuffer src) {
    setContentSize(bb, src.remaining());
    src.mark();
    bb.setBytes(CONTENT_OFFSET, src);
    src.reset();
  }
}
