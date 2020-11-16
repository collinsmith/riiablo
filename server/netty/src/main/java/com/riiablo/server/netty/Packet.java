package com.riiablo.server.netty;

import io.netty.buffer.ByteBuf;
import java.nio.ByteBuffer;
import org.apache.commons.lang3.Validate;

import com.badlogic.gdx.utils.Bits;

public class Packet {
  static final int SINGLE = 0;
  static final int FRAGMENTED = 1;
  static final int SLICED = 2;
  static final int SLICEDACK = 3;
  static final int MAX_VALUE = SLICEDACK;

  static final int PROTOCOL_OFFSET = 0;
  static final int PROTOCOL_SIZE = 1; // ubyte

  static final int TYPE_OFFSET = PROTOCOL_OFFSET + PROTOCOL_SIZE;
  static final int TYPE_SIZE = 1; // ubyte

  static final int SEQ_OFFSET = TYPE_OFFSET + TYPE_SIZE;
  static final int SEQ_SIZE = 2; // ushort

  public static int getProtocol(ByteBuf bb) {
    return bb.getUnsignedByte(PROTOCOL_OFFSET);
  }

  public static int getType(ByteBuf bb) {
    return bb.getUnsignedByte(TYPE_OFFSET);
  }

  public static int getSEQ(ByteBuf bb) {
    return bb.getUnsignedShort(SEQ_OFFSET);
  }

  static void setProtocol(ByteBuf bb, int value) {
    bb.setByte(PROTOCOL_OFFSET, value);
  }

  static void setType(ByteBuf bb, int value) {
    assert 0 <= value && value <= MAX_VALUE;
    bb.setByte(TYPE_OFFSET, value);
  }

  static void setSEQ(ByteBuf bb, int value) {
    bb.setShort(SEQ_OFFSET, value);
  }

  static String toString(ByteBuf bb) {
    return String.format("PROTO:%d TYPE:%d SEQ:%d",
        getProtocol(bb), getType(bb), getSEQ(bb));
  }

  static class Single extends Packet {
    static final int ACK_OFFSET = SEQ_OFFSET + SEQ_SIZE;
    static final int ACK_SIZE = 2; // ushort

    static final int ACK_BITS_OFFSET = ACK_OFFSET + ACK_SIZE;
    static final int ACK_BITS_SIZE = 4; // int

    static final int CONTENT_SIZE_OFFSET = ACK_BITS_OFFSET + ACK_BITS_SIZE;
    static final int CONTENT_SIZE_SIZE = 2; // ushort

    static final int CONTENT_OFFSET = CONTENT_SIZE_OFFSET + CONTENT_SIZE_SIZE;

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
      return String.format("%s ACK:%d ACK_BITS:%08x CSIZE:%d",
          Packet.toString(bb), getACK(bb), getACK_BITS(bb), getContentSize(bb));
    }
  }

  static class Fragmented extends Packet {
    static final int FRAGID_OFFSET = SEQ_OFFSET + SEQ_SIZE;
    static final int FRAGID_SIZE = 1; // ubyte

    static final int NUMFRAG_OFFSET = FRAGID_OFFSET + FRAGID_SIZE;
    static final int NUMFRAG_SIZE = 1; // ubyte

    static final int FRAGSIZE_OFFSET = NUMFRAG_OFFSET + NUMFRAG_SIZE;
    static final int FRAGSIZE_SIZE = 2; // ushort

    static final int CONTENT_OFFSET = FRAGSIZE_OFFSET + FRAGSIZE_SIZE;

    public static int getFragmentId(ByteBuf bb) {
      return bb.getUnsignedByte(FRAGID_OFFSET);
    }

    public static int getNumFragments(ByteBuf bb) {
      return bb.getUnsignedByte(NUMFRAG_OFFSET);
    }

    public static int getFragmentSize(ByteBuf bb) {
      return bb.getUnsignedShort(FRAGSIZE_OFFSET);
    }

    public static ByteBuf getHeader(ByteBuf bb) {
      return bb.slice(0, CONTENT_OFFSET);
    }

    public static ByteBuf getFragment(ByteBuf bb) {
      return bb.slice(CONTENT_OFFSET, getFragmentSize(bb));
    }

    static void setFragmentId(ByteBuf bb, int value) {
      bb.setByte(FRAGID_OFFSET, value);
    }

    static void setNumFragments(ByteBuf bb, int value) {
      bb.setByte(NUMFRAG_OFFSET, value);
    }

    static void setFragmentSize(ByteBuf bb, int value) {
      bb.setShort(FRAGSIZE_OFFSET, value);
    }

    static void createHeader(ByteBuf bb, int protocol, int seq, int fragId, int numFrags) {
      bb.writerIndex(CONTENT_OFFSET);
      setProtocol(bb, protocol);
      setSEQ(bb, seq);
      setFragmentId(bb, fragId);
      setNumFragments(bb, numFrags);
    }

    static String toString(ByteBuf bb) {
      return String.format("%s FRAGID:%d NUMFRAGS:%d FSIZE:%d",
          Packet.toString(bb), getFragmentId(bb), getNumFragments(bb), getFragmentSize(bb));
    }
  }

  static class Sliced extends Packet {
    static final int CHUNKID_OFFSET = SEQ_OFFSET + SEQ_SIZE;
    static final int CHUNKID_SIZE = 1; // ubyte

    static final int SLICEID_OFFSET = CHUNKID_OFFSET + CHUNKID_SIZE;
    static final int SLICEID_SIZE = 1; // ubyte

    static final int NUMSLICE_OFFSET = SLICEID_OFFSET + SLICEID_SIZE;
    static final int NUMSLICE_SIZE = 1; // ubyte

    static final int SLICESIZE_OFFSET = NUMSLICE_OFFSET + NUMSLICE_SIZE;
    static final int SLICESIZE_SIZE = 2; // ushort

    static final int CONTENT_OFFSET = SLICESIZE_OFFSET + SLICESIZE_SIZE;

    static final int MAX_SLICE_SIZE = 1 << 10;

    public static int getChunkId(ByteBuf bb) {
      return bb.getUnsignedByte(CHUNKID_OFFSET);
    }

    public static int getSliceId(ByteBuf bb) {
      return bb.getUnsignedByte(SLICEID_OFFSET);
    }

    public static int getNumSlices(ByteBuf bb) {
      return bb.getUnsignedByte(NUMSLICE_OFFSET);
    }

    public static int getSliceSize(ByteBuf bb) {
      return bb.getUnsignedShort(SLICESIZE_OFFSET);
    }

    public static ByteBuf getHeader(ByteBuf bb) {
      return bb.slice(0, CONTENT_OFFSET);
    }

    public static ByteBuf getSlice(ByteBuf bb) {
      return bb.slice(CONTENT_OFFSET, getSliceSize(bb));
    }

    static String toString(ByteBuf bb) {
      return String.format("%s CHUNKID:%d SLICEID:%d NUMSLICES:%d SSIZE:%d",
          Packet.toString(bb), getChunkId(bb), getSliceId(bb), getNumSlices(bb), getSliceSize(bb));
    }
  }

  static class SlicedAck extends Packet {
    static final int CHUNKID_OFFSET = SEQ_OFFSET + SEQ_SIZE;
    static final int CHUNKID_SIZE = 1; // ubyte

    static final int NUMSLICE_OFFSET = CHUNKID_OFFSET + CHUNKID_SIZE;
    static final int NUMSLICE_SIZE = 1; // ubyte

    static final int ACK_BITS_OFFSET = NUMSLICE_OFFSET + NUMSLICE_SIZE;
    static final int ACK_BITS_SIZE = 32; // array

    static final int CONTENT_OFFSET = ACK_BITS_OFFSET + ACK_BITS_SIZE;

    public static int getChunkId(ByteBuf bb) {
      return bb.getUnsignedByte(CHUNKID_OFFSET);
    }

    public static int getNumSlices(ByteBuf bb) {
      return bb.getUnsignedByte(NUMSLICE_OFFSET);
    }

    public static Bits getACK_BITS(ByteBuf bb) {
      throw new UnsupportedOperationException();
    }

    public static ByteBuf getHeader(ByteBuf bb) {
      return bb;
    }

    static String toString(ByteBuf bb) {
      return String.format("%s CHUNKID:%d NUMSLICES:%d ACK_BITS:NULL",
          Packet.toString(bb), getChunkId(bb), getNumSlices(bb));
    }
  }
}
