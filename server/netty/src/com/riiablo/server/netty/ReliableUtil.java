package com.riiablo.server.netty;

public class ReliableUtil {
  private ReliableUtil() {}
/*
  static final int PACKET_SINGLE   = 0;
  static final int PACKET_FRAGMENT = 1;
  static final int PACKET_SLICE    = 2;
  static final int PACKET_SLICEACK = 3;

  private static final int PROTOCOL_OFFSET = 0;
  private static final int PROTOCOL_SIZE   = 1; // ubyte

  private static final int TYPE_OFFSET     = PROTOCOL_OFFSET + PROTOCOL_SIZE;
  private static final int TYPE_SIZE       = 1; // ubyte

  private static final int SEQ_OFFSET      = TYPE_OFFSET + TYPE_SIZE;
  private static final int SEQ_SIZE        = 2; // ushort

  static class TYPE0 {
    private static final int ACK_OFFSET          = SEQ_OFFSET + SEQ_SIZE;
    private static final int ACK_SIZE            = 2; // ushort

    private static final int ACK_BITS_OFFSET     = ACK_OFFSET + ACK_SIZE;
    private static final int ACK_BITS_SIZE       = 4; // int

    private static final int CONTENT_SIZE_OFFSET = ACK_BITS_OFFSET + ACK_BITS_SIZE;
    private static final int CONTENT_SIZE_SIZE   = 2; // ushort

    static final int CONTENT_OFFSET = CONTENT_SIZE_OFFSET + CONTENT_SIZE_SIZE;
  }

  static class TYPE1 {
    private static final int FRAGID_OFFSET       = SEQ_OFFSET + SEQ_SIZE;
    private static final int FRAGID_SIZE         = 1; // ubyte

    private static final int NUMFRAG_OFFSET      = FRAGID_OFFSET + FRAGID_SIZE;
    private static final int NUMFRAG_SIZE        = 1; // ubyte

    private static final int FRAG_SIZE_OFFSET    = NUMFRAG_OFFSET + NUMFRAG_SIZE;
    private static final int FRAG_SIZE_SIZE      = 2; // ushort

    static final int CONTENT_OFFSET = FRAG_SIZE_OFFSET + FRAG_SIZE_SIZE;
  }

  static class TYPE2 {
    private static final int CHUNKID_OFFSET      = SEQ_OFFSET + SEQ_SIZE;
    private static final int CHUNKID_SIZE        = 1; // ubyte

    private static final int SLICEID_OFFSET      = CHUNKID_OFFSET + CHUNKID_SIZE;
    private static final int SLICEID_SIZE        = 1; // ubyte

    private static final int NUMSLICE_OFFSET     = SLICEID_OFFSET + SLICEID_SIZE;
    private static final int NUMSLICE_SIZE       = 1; // ubyte

    private static final int SLICE_SIZE_OFFSET   = NUMSLICE_OFFSET + NUMSLICE_SIZE;
    private static final int SLICE_SIZE_SIZE     = 2; // ushort

    static final int CONTENT_OFFSET = SLICE_SIZE_OFFSET + SLICE_SIZE_SIZE;
  }

  static class TYPE3 {
    private static final int CHUNKID_OFFSET      = SEQ_OFFSET + SEQ_SIZE;
    private static final int CHUNKID_SIZE        = 1; // ubyte

    private static final int NUMSLICE_OFFSET     = CHUNKID_OFFSET + CHUNKID_SIZE;
    private static final int NUMSLICE_SIZE       = 1; // ubyte

    private static final int ACK_BITS_OFFSET     = NUMSLICE_OFFSET + NUMSLICE_SIZE;
    private static final int ACK_BITS_SIZE       = 32; // array

    static final int CONTENT_OFFSET = ACK_BITS_OFFSET + ACK_BITS_SIZE;
  }

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

  public static ByteBuf getHeader(ByteBuf bb) {
    return bb.slice(0, CONTENT_OFFSET);
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

  static void createHeader(ByteBuf bb, int protocol, int seq, int ack, int ack_bits) {
    bb.writerIndex(CONTENT_OFFSET);
    setProtocol(bb, protocol);
    setSEQ(bb, seq);
    setACK(bb, ack);
    setACK_BITS(bb, ack_bits);
  }

  static String toString(ByteBuf bb) {
    return String.format("PROTO:%d SEQ:%d ACK:%d ACK_BITS:%08x CSIZE:%d",
        getProtocol(bb), getSEQ(bb), getACK(bb), getACK_BITS(bb), getContentSize(bb));
  }
  */
}
