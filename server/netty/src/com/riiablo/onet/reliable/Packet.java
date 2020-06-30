package com.riiablo.onet.reliable;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

public class Packet {
  private static final String TAG = "Packet";

  public static final int USHORT_MAX_VALUE = 0xFFFF;

  public static final int MAX_PACKET_HEADER_SIZE = 10;
  public static final int FRAGMENT_HEADER_SIZE = 6;

  static final int SINGLE     = 0;
  static final int FRAGMENTED = 1 << 0;
  static final int ACK_BYTE0  = 1 << 1;
  static final int ACK_BYTE1  = 1 << 2;
  static final int ACK_BYTE2  = 1 << 3;
  static final int ACK_BYTE3  = 1 << 4;
  static final int SEQ_DIFF   = 1 << 5;
  static final int ACK        = 1 << 7;

  static final int TYPE_MASK  = SINGLE | FRAGMENTED;

  static final int ACK_BYTE0_MASK = 0x000000FF;
  static final int ACK_BYTE1_MASK = 0x0000FF00;
  static final int ACK_BYTE2_MASK = 0x00FF0000;
  static final int ACK_BYTE3_MASK = 0xFF000000;

  private static final int FLAGS_OFFSET   = 0;
  private static final int CHANNEL_OFFSET = 1;

  public static byte getFlags(ByteBuf bb) {
    return bb.getByte(FLAGS_OFFSET);
  }

  public static int getChannelId(ByteBuf bb) {
    return bb.getUnsignedByte(CHANNEL_OFFSET);
  }

  private Packet() {}

  public static boolean isFragmented(byte flags) {
    return (flags & TYPE_MASK) == FRAGMENTED;
  }

  public static boolean isAck(byte flags) {
    return (flags & ACK) == ACK;
  }

  private static int getAckBitsFlags(int ackBits, int prefixByte) {
    if ((ackBits & ACK_BYTE0_MASK) != ACK_BYTE0_MASK) prefixByte |= ACK_BYTE0;
    if ((ackBits & ACK_BYTE1_MASK) != ACK_BYTE1_MASK) prefixByte |= ACK_BYTE1;
    if ((ackBits & ACK_BYTE2_MASK) != ACK_BYTE2_MASK) prefixByte |= ACK_BYTE2;
    if ((ackBits & ACK_BYTE3_MASK) != ACK_BYTE3_MASK) prefixByte |= ACK_BYTE3;
    return prefixByte;
  }

  private static void writeAckBitsFlags(ByteBuf bb, int ackBits) {
    if ((ackBits & ACK_BYTE0_MASK) != ACK_BYTE0_MASK) bb.writeByte(ackBits);
    if ((ackBits & ACK_BYTE1_MASK) != ACK_BYTE1_MASK) bb.writeByte(ackBits >> 8);
    if ((ackBits & ACK_BYTE2_MASK) != ACK_BYTE2_MASK) bb.writeByte(ackBits >> 16);
    if ((ackBits & ACK_BYTE3_MASK) != ACK_BYTE3_MASK) bb.writeByte(ackBits >> 24);
  }

  public static int writeAck(ByteBuf bb, int channelId, int ack, int ackBits) {
    int startIndex = bb.writerIndex();
    final int flags = getAckBitsFlags(ackBits, ACK);
    bb.writeByte(flags);
    bb.writeByte(channelId);
    bb.writeShortLE(ack);
    writeAckBitsFlags(bb, ackBits);
    return bb.writerIndex() - startIndex;
  }

  public static int writePacketHeader(ByteBuf bb, int channelId, int sequence, int ack, int ackBits) {
    int startIndex = bb.writerIndex();
    int flags = getAckBitsFlags(ackBits, SINGLE);

    int sequenceDiff = sequence - ack;
    if (sequenceDiff < 0) sequenceDiff += USHORT_MAX_VALUE;
    if (sequenceDiff <= 0xFF) flags |= SEQ_DIFF;

    bb.writeByte(flags);
    bb.writeByte(channelId);
    bb.writeShortLE(sequence);

    if (sequenceDiff <= 0xFF) {
      bb.writeByte(sequenceDiff);
    } else {
      bb.writeShortLE(ack);
    }

    writeAckBitsFlags(bb, ackBits);
    return bb.writerIndex() - startIndex;
  }

  public static int readPacketHeader(ReliableConfiguration config, ByteBuf bb, HeaderData out) {
    assert out != null;
    int startIndex = bb.readerIndex();
    if (bb.readableBytes() < 4) {
      Log.error(TAG, "buffer too small for packet header (1)");
      return out.headerSize = -1;
    }

    final byte flags = out.flags = bb.readByte();
    if ((flags & TYPE_MASK) != SINGLE) {
      Log.error(TAG, "packet header not flagged as single packet");
      return out.headerSize = -1;
    }

    out.channelId = bb.readUnsignedByte();
    out.sequence = (flags & ACK) == ACK ? 0 : bb.readUnsignedShortLE(); // ACK doesn't have sequence

    if ((flags & SEQ_DIFF) == SEQ_DIFF) {
      if (bb.readableBytes() < 1) {
        Log.error(TAG, "buffer too small for packet header (2)");
        return out.headerSize = -1;
      }

      int sequenceDiff = bb.readUnsignedByte();
      out.ack = (out.sequence - sequenceDiff) & USHORT_MAX_VALUE;
    } else {
      if (bb.readableBytes() < 2) {
        Log.error(TAG, "buffer too small for packet header (3)");
        return out.headerSize = -1;
      }

      out.ack = bb.readUnsignedShortLE();
    }

    int expectedBytes = 0;
    for (int i = ACK_BYTE0; i <= ACK_BYTE3; i <<= 1) {
      if ((flags & i) == i) expectedBytes++;
    }

    if (bb.readableBytes() < expectedBytes) {
      Log.error(TAG, "buffer too small for packet header (4)");
      return out.headerSize = -1;
    }

    int ackBits = 0xFFFFFFFF;
    if ((flags & ACK_BYTE0) == ACK_BYTE0) {
      ackBits &= ~ACK_BYTE0_MASK;
      ackBits |= bb.readByte();
    }
    if ((flags & ACK_BYTE1) == ACK_BYTE1) {
      ackBits &= ~ACK_BYTE1_MASK;
      ackBits |= (bb.readByte() << 8);
    }
    if ((flags & ACK_BYTE2) == ACK_BYTE2) {
      ackBits &= ~ACK_BYTE2_MASK;
      ackBits |= (bb.readByte() << 16);
    }
    if ((flags & ACK_BYTE3) == ACK_BYTE3) {
      ackBits &= ~ACK_BYTE3_MASK;
      ackBits |= (bb.readByte() << 24);
    }

    out.ackBits = ackBits;
    return out.headerSize = bb.readerIndex() - startIndex;
  }

  public static int readFragmentHeader(ReliableConfiguration config, ByteBuf bb, FragmentedHeaderData out) {
    assert out != null;
    int startIndex = bb.readerIndex();
    if (bb.readableBytes() < FRAGMENT_HEADER_SIZE) {
      Log.error(TAG, "buffer too small for fragment header");
      return out.headerSize = -1;
    }

    final byte flags = out.flags = bb.readByte();
    if ((flags & TYPE_MASK) != FRAGMENTED) {
      Log.error(TAG, "packet header not flagged as fragmented packet");
      return out.headerSize = -1;
    }

    final int channelId = out.channelId = bb.readUnsignedByte();
    final int sequence = out.sequence = bb.readUnsignedShortLE();

    final int fragmentId = out.fragmentId = bb.readUnsignedByte();
    final int numFragments = out.numFragments = bb.readUnsignedByte();

    if (numFragments > config.maxFragments) {
      Log.error(TAG, "num fragments %d outside of range of max fragments %d", numFragments, config.maxFragments);
      return out.headerSize = -1;
    }

    if (fragmentId >= numFragments) {
      Log.error(TAG, "fragment id %d outside of range of num fragments %d", fragmentId, numFragments);
      return out.headerSize = -1;
    }

    if (fragmentId == 0) {
      if (bb.readableBytes() < 1) {
        Log.error(TAG, "buffer too small for packet header");
        return out.headerSize = -1;
      }

      final HeaderData packetHeader = out.header;
      int headerSize = readPacketHeader(config, bb, packetHeader);
      if (headerSize < 0) {
        Log.error(TAG, "bad packet header in fragment");
        return out.headerSize = -1;
      }

      if (packetHeader.sequence != sequence) {
        Log.error(TAG, "bad packet sequence in fragment. expected %d, got %d", sequence, packetHeader.sequence);
        return out.headerSize = -1;
      }

      out.ack = packetHeader.ack;
      out.ackBits = packetHeader.ackBits;
    } else {
      out.ack = 0;
      out.ackBits = 0;
    }

    final int fragmentSize = out.fragmentSize = bb.readableBytes();
    if (fragmentSize > config.fragmentSize) {
      Log.error(TAG, "fragment bytes %d > fragment size %d", fragmentSize, config.fragmentSize);
      return out.headerSize = -1;
    }

    if (fragmentId != numFragments - 1 && fragmentSize != config.fragmentSize) {
      Log.error(TAG, "fragment %d is %d bytes, which is not the expected fragment size %d bytes", fragmentId, fragmentSize, config.fragmentSize);
      return out.headerSize = -1;
    }

    return out.headerSize = bb.readerIndex() - startIndex;
  }

  private static final Pool<HeaderData> HEADER_DATA_POOL = Pools.get(HeaderData.class);

  public static HeaderData obtainData() {
    return HEADER_DATA_POOL.obtain();
  }

  public static class HeaderData {
    public byte flags;
    public int  channelId;
    public int  sequence;
    public int  ack;
    public int  ackBits;
    public int  headerSize;

    public void free() {
      HEADER_DATA_POOL.free(this);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("flags", String.format("%02x", flags))
          .append("channelId", channelId)
          .append("sequence", sequence)
          .append("ack", ack)
          .append("ackBits", String.format("%08x", ackBits))
          .append("headerSize", headerSize)
          .build();
    }
  }

  private static final Pool<FragmentedHeaderData> FRAGMENTED_HEADER_DATA_POOL = Pools.get(FragmentedHeaderData.class);

  public static FragmentedHeaderData obtainFragmentedData() {
    return FRAGMENTED_HEADER_DATA_POOL.obtain();
  }

  public static class FragmentedHeaderData {
    public byte flags;
    public int  channelId;
    public int  sequence;
    public int  ack;
    public int  ackBits;
    public int  headerSize;

    public int  fragmentId;
    public int  numFragments;
    public int  fragmentSize;

    public final HeaderData header = new HeaderData();

    public void free() {
      FRAGMENTED_HEADER_DATA_POOL.free(this);
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("flags", String.format("%02x", flags))
          .append("channelId", channelId)
          .append("sequence", sequence)
          .append("ack", ack)
          .append("ackBits", String.format("%08x", ackBits))
          .append("headerSize", headerSize)

          .append("fragmentId", fragmentId)
          .append("numFragments", numFragments)
          .append("fragmentSize", fragmentSize)

          .append("header", header)
          .build();
    }
  }
}
