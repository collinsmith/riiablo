package com.riiablo.mpq_bytebuf.util;

import io.netty.buffer.ByteBuf;

public class ADPCM {
  private ADPCM() {}

  private static final byte INITIAL_ADPCM_STEP_INDEX = 0x2C;

  static final byte[] CHANGE_TABLE = {
      -1, 0, -1, 4, -1, 2, -1, 6,
      -1, 1, -1, 5, -1, 3, -1, 7,
      -1, 1, -1, 5, -1, 3, -1, 7,
      -1, 2, -1, 4, -1, 6, -1, 8
  };

  static final short[] STEP_SIZE = {
      0x0007, 0x0008, 0x0009, 0x000a, 0x000b, 0x000c, 0x000d, 0x000e,
      0x0010, 0x0011, 0x0013, 0x0015, 0x0017, 0x0019, 0x001c, 0x001f,
      0x0022, 0x0025, 0x0029, 0x002d, 0x0032, 0x0037, 0x003c, 0x0042,
      0x0049, 0x0050, 0x0058, 0x0061, 0x006b, 0x0076, 0x0082, 0x008f,
      0x009d, 0x00ad, 0x00be, 0x00d1, 0x00e6, 0x00fd, 0x0117, 0x0133,
      0x0151, 0x0173, 0x0198, 0x01c1, 0x01ee, 0x0220, 0x0256, 0x0292,
      0x02d4, 0x031c, 0x036c, 0x03c3, 0x0424, 0x048e, 0x0502, 0x0583,
      0x0610, 0x06ab, 0x0756, 0x0812, 0x08e0, 0x09c3, 0x0abd, 0x0bd0,
      0x0cff, 0x0e4c, 0x0fba, 0x114c, 0x1307, 0x14ee, 0x1706, 0x1954,
      0x1bdc, 0x1ea5, 0x21b6, 0x2515, 0x28ca, 0x2cdf, 0x315b, 0x364b,
      0x3bb9, 0x41b2, 0x4844, 0x4f7e, 0x5771, 0x602f, 0x69ce, 0x7462,
      0x7fff,
  };

  public static final int MONO = 1;
  public static final int STEREO = 2;
  public static final int CHANNELS = 2;

  static final class Channel {
    short sampleValue;
    byte stepIndex;
  }

  public static int decode(final ByteBuf in, final ByteBuf out, final int numChannels) {
    final Channel[] state = new Channel[numChannels];
    for (int i = 0; i < numChannels; i++) state[i] = new Channel();

    byte stepshift = (byte) (in.readShortLE() >>> Byte.SIZE);
    for (int i = 0; i < numChannels; i++) {
      Channel c = state[i];
      c.stepIndex = INITIAL_ADPCM_STEP_INDEX;
      c.sampleValue = in.readShortLE();
      out.writeShortLE(c.sampleValue);
    }

    int current = 0;
    while (in.readableBytes() > 0) {
      byte op = in.readByte();
      Channel channel = state[current];
      if ((op & 0x80) != 0) {
        switch (op & 0x7F) {
          case 0: // write current value
            if (channel.stepIndex != 0) channel.stepIndex--;
            out.writeShortLE(channel.sampleValue);
            current = (current + 1) % numChannels;
            break;
          case 1: // increment period
            channel.stepIndex += 8;
            if (channel.stepIndex >= STEP_SIZE.length)
              channel.stepIndex = (byte) (STEP_SIZE.length - 1);
            break;
          case 2: // skip channel (unused?)
            current = (current + 1) % numChannels;
            break;
          default:
            channel.stepIndex -= 8;
            if (channel.stepIndex < 0) channel.stepIndex = 0;
        }
      } else { // adjust value
        short stepbase = STEP_SIZE[channel.stepIndex];
        short step = (short) (stepbase >>> stepshift);
        for (int i = 0; i < 6; i++) {
          if ((op & 1 << i) != 0) {
            step += (stepbase >> i);
          }
        }

        if ((op & 0x40) != 0) {
          channel.sampleValue = (short) Math.max((int) channel.sampleValue - step, Short.MIN_VALUE);
        } else {
          channel.sampleValue = (short) Math.min((int) channel.sampleValue + step, Short.MAX_VALUE);
        }

        out.writeShortLE(channel.sampleValue);
        channel.stepIndex += CHANGE_TABLE[op & 0x1F];
        if (channel.stepIndex < 0) {
          channel.stepIndex = 0;
        } else if (channel.stepIndex >= STEP_SIZE.length) {
          channel.stepIndex = (byte) (STEP_SIZE.length - 1);
        }

        current = (current + 1) % numChannels;
      }
    }

    return out.writerIndex();
  }
}
