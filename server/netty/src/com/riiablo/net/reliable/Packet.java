package com.riiablo.net.reliable;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.Gdx;

public abstract class Packet {
  private static final String TAG = "Packet";
  
  static final int MAX_PACKET_HEADER_SIZE = 10;
  static final int FRAGMENT_HEADER_SIZE = 6;

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

  public static boolean isSinglePacket(int prefixByte) {
    return (prefixByte & TYPE_MASK) == SINGLE;
  }

  static int getAckBitByteFlags(int ackBits, int prefixByte) {
    if ((ackBits & ACK_BYTE0_MASK) != ACK_BYTE0_MASK) prefixByte |= ACK_BYTE0;
    if ((ackBits & ACK_BYTE1_MASK) != ACK_BYTE1_MASK) prefixByte |= ACK_BYTE1;
    if ((ackBits & ACK_BYTE2_MASK) != ACK_BYTE2_MASK) prefixByte |= ACK_BYTE2;
    if ((ackBits & ACK_BYTE3_MASK) != ACK_BYTE3_MASK) prefixByte |= ACK_BYTE3;
    return prefixByte;
  }

  static void writeAckBitByteFlags(ByteBuf bb, int ackBits) {
    if ((ackBits & ACK_BYTE0_MASK) != ACK_BYTE0_MASK) bb.writeByte(ackBits);
    if ((ackBits & ACK_BYTE1_MASK) != ACK_BYTE1_MASK) bb.writeByte(ackBits >> 8);
    if ((ackBits & ACK_BYTE2_MASK) != ACK_BYTE2_MASK) bb.writeByte(ackBits >> 16);
    if ((ackBits & ACK_BYTE3_MASK) != ACK_BYTE3_MASK) bb.writeByte(ackBits >> 24);
  }

  static void logError(String format, Object... args) {
    logError(String.format(format, args));
  }

  static void logError(String format) {
    Gdx.app.error(TAG, format);
  }

  // Used for debugging purposes
  @Deprecated
  public static Packet readHeader(ReliableConfig config, ByteBuf bb) {
    if (bb.readableBytes() < 1) {
      logError("buffer too small for packet header");
      return null;
    }

    byte prefixByte = bb.readByte();
    Packet packet;
    if (isSinglePacket(prefixByte)) {
      packet = new SinglePacket();
    } else {
      packet = new FragmentedPacket();
    }

    packet.readHeader(config, bb, prefixByte);
    return packet;
  }

  abstract int readHeader(ReliableConfig config, ByteBuf bb, final byte prefixByte);

  static class SinglePacket extends Packet {
    public int channelId;
    public int sequence;
    public int ack;
    public int ackBits;

    int headerSize;

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("channelId", channelId)
          .append("sequence", sequence)
          .append("ack", ack)
          .append("ackBits", String.format("%08x", ackBits))
          .append("headerSize", headerSize)
          .build();
    }

    @Override
    int readHeader(ReliableConfig config, ByteBuf bb, byte prefixByte) {
      if ((prefixByte & TYPE_MASK) != SINGLE) {
        logError("packet header not flagged as single packet");
        return -1;
      }

      int startIndex = bb.readerIndex();

      if (bb.readableBytes() < 3) {
        logError("buffer too small for packet header (1)");
        return -1;
      }
      channelId = bb.readUnsignedByte();

      // ack packets don't have sequence numbers
      sequence = (prefixByte & ACK) == ACK ? 0 : bb.readUnsignedShortLE();

      if ((prefixByte & SEQ_DIFF) == SEQ_DIFF) {
        if (bb.readableBytes() < 1) {
          logError("buffer too small for packet header (2)");
          return -1;
        }
        int sequenceDiff = bb.readUnsignedByte();
        ack = (sequence - sequenceDiff) & 0xFFFF;
      } else {
        if (bb.readableBytes() < 2) {
          logError("buffer too small for packet header (3)");
          return -1;
        }
        ack = bb.readUnsignedShortLE();
      }

      int expectedBytes = 0;
      for (int i = ACK_BYTE0; i <= ACK_BYTE3; i <<= 1) {
        if ((prefixByte & i) == i) expectedBytes++;
      }

      if (bb.readableBytes() < expectedBytes) {
        logError("buffer too small for packet header (4)");
        return -1;
      }
      ackBits = 0xFFFFFFFF;
      if ((prefixByte & ACK_BYTE0) == ACK_BYTE0) {
        ackBits &= ~ACK_BYTE0_MASK;
        ackBits |= bb.readByte();
      }
      if ((prefixByte & ACK_BYTE1) == ACK_BYTE1) {
        ackBits &= ~ACK_BYTE1_MASK;
        ackBits |= (bb.readByte() << 8);
      }
      if ((prefixByte & ACK_BYTE2) == ACK_BYTE2) {
        ackBits &= ~ACK_BYTE2_MASK;
        ackBits |= (bb.readByte() << 16);
      }
      if ((prefixByte & ACK_BYTE3) == ACK_BYTE3) {
        ackBits &= ~ACK_BYTE3_MASK;
        ackBits |= (bb.readByte() << 24);
      }

      headerSize = bb.readerIndex() - startIndex + 1; // include prefixByte
      return headerSize;
    }

    static void writeHeader(ByteBuf bb, int channelId, int sequence, int ack, int ackBits) {
      int prefixByte = getAckBitByteFlags(ackBits, 0);

      int sequenceDiff = sequence - ack;
      if (sequenceDiff < 0) sequenceDiff += 0xFFFF;
      if (sequenceDiff <= 0xFF) prefixByte |= SEQ_DIFF;

      bb.writeByte(prefixByte);
      bb.writeByte(channelId);
      bb.writeShortLE(sequence);

      if (sequenceDiff <= 0xFF) {
        bb.writeByte(sequenceDiff);
      } else {
        bb.writeShortLE(ack);
      }

      writeAckBitByteFlags(bb, ackBits);
    }
  }

  static class FragmentedPacket extends Packet {
    public int channelId;
    public int sequence;
    public int ack;
    public int ackBits;

    public int fragmentId;
    public int numFragments;

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("channelId", channelId)
          .append("sequence", sequence)
          .append("ack", ack)
          .append("ackBits", String.format("%08x", ackBits))
          .append("fragmentId", fragmentId)
          .append("numFragments", numFragments)
          .build();
    }

    @Override
    int readHeader(ReliableConfig config, ByteBuf bb, final byte prefixByte) {
      if ((prefixByte & TYPE_MASK) != FRAGMENTED) {
        logError("packet header not flagged as fragmented packet");
        return -1;
      }

      if (bb.readableBytes() < FRAGMENT_HEADER_SIZE) {
        logError("buffer too small for fragment header");
        return -1;
      }
      channelId = bb.readUnsignedByte();
      sequence = bb.readUnsignedShortLE();

      fragmentId = bb.readUnsignedByte();
      numFragments = bb.readUnsignedByte() + 1;

      if (numFragments > config.maxFragments) {
        logError("num fragments %d outside of range of max fragments %d", numFragments, config.maxFragments);
        return -1;
      }
      if (fragmentId >= numFragments) {
        logError("fragment id %d outside of range of num fragments %d", fragmentId, numFragments);
        return -1;
      }

      if (fragmentId == 0) {
        if (bb.readableBytes() < 1) {
          logError("buffer too small for packet header");
          return -1;
        }
        SinglePacket packetHeader = new SinglePacket();
        int packetHeaderSize = packetHeader.readHeader(config, bb, bb.readByte());
        if (packetHeaderSize < 0) {
          logError("bad packet header in fragment");
          return -1;
        }
        if (packetHeader.sequence != sequence) {
          logError("bad packet sequence in fragment. expected %d, got %d", sequence, packetHeader.sequence);
          return -1;
        }
        ack = packetHeader.ack;
        ackBits = packetHeader.ackBits;
      } else {
        ack = 0;
        ackBits = 0;
      }

      // TODO: validate fragmentBytes <= fragmentSize
      // TODO: validate size of fragmentId == fragmentSize

      // TODO: implement support
      int headerSize = 0;
      return headerSize;
    }
  }

  static class AckPacket {
    static void writeHeader(ByteBuf bb, int channelId, int ack, int ackBits) {
      int prefixByte = getAckBitByteFlags(ackBits, ACK);

      bb.writeByte(prefixByte);
      bb.writeByte(channelId);
      bb.writeShortLE(ack);

      writeAckBitByteFlags(bb, ackBits);
    }
  }
}
