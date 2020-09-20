package com.riiablo.mpq_bytebuf.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;

import com.badlogic.gdx.utils.Pool;

public final class ADPCM {
  private ADPCM() {}

  private static final byte INITIAL_ADPCM_STEP_INDEX = 0x2C;

  private static final byte[] CHANGE_TABLE = {
      -1, 0, -1, 4, -1, 2, -1, 6,
      -1, 1, -1, 5, -1, 3, -1, 7,
      -1, 1, -1, 5, -1, 3, -1, 7,
      -1, 2, -1, 4, -1, 6, -1, 8
  };

  private static final short[] STEP_TABLE = {
      7,     8,     9,     10,    11,    12,    13,    14,
      16,    17,    19,    21,    23,    25,    28,    31,
      34,    37,    41,    45,    50,    55,    60,    66,
      73,    80,    88,    97,    107,   118,   130,   143,
      157,   173,   190,   209,   230,   253,   279,   307,
      337,   371,   408,   449,   494,   544,   598,   658,
      724,   796,   876,   963,   1060,  1166,  1282,  1411,
      1552,  1707,  1878,  2066,  2272,  2499,  2749,  3024,
      3327,  3660,  4026,  4428,  4871,  5358,  5894,  6484,
      7132,  7845,  8630,  9493,  10442, 11487, 12635, 13899,
      15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794,
      32767
  };

  static final int MONO = 1;
  static final int STEREO = 2;
  private static final int CHANNELS = 2;

  private static final class Channel {
    short sampleValue;
    byte stepIndex;
  }

  private static final Pool<Channel[]> POOL = new Pool<Channel[]>(8, 64, true) {
    @Override
    protected Channel[] newObject() {
      final Channel[] channels = new Channel[CHANNELS];
      for (int i = 0; i < CHANNELS; i++) channels[i] = new Channel();
      return channels;
    }
  };

  private static final ByteBufAllocator ALLOC = PooledByteBufAllocator.DEFAULT;

  public static int decompress(final ByteBuf inout, final int numChannels) {
    // NOTE: in must be copied because it will eventually reach out
    final ByteBuf in = ALLOC.heapBuffer(inout.resetReaderIndex().readableBytes());
    try {
      in.writeBytes(inout);
      return decompress(in, inout.clear(), numChannels);
    } finally {
      in.release();
    }
  }

  public static int decompress(final ByteBuf in, final ByteBuf out, final int numChannels) {
    Channel[] channels = POOL.obtain();
    try {
      return decompress(in, out, numChannels, channels);
    } finally {
      POOL.free(channels);
    }
  }

  public static int decompress(final ByteBuf in, final ByteBuf out, final int numChannels, final Channel[] state) {
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
            if (channel.stepIndex >= STEP_TABLE.length)
              channel.stepIndex = (byte) (STEP_TABLE.length - 1);
            break;
          case 2: // skip channel (unused?)
            current = (current + 1) % numChannels;
            break;
          default:
            channel.stepIndex -= 8;
            if (channel.stepIndex < 0) channel.stepIndex = 0;
        }
      } else { // adjust value
        short stepbase = STEP_TABLE[channel.stepIndex];
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
        } else if (channel.stepIndex >= STEP_TABLE.length) {
          channel.stepIndex = (byte) (STEP_TABLE.length - 1);
        }

        current = (current + 1) % numChannels;
      }
    }

    return out.writerIndex();
  }
}
