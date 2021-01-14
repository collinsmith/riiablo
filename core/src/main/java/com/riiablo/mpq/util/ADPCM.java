package com.riiablo.mpq.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.badlogic.gdx.utils.Pool;

public class ADPCM {
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

  private static final int CHANNELS = 2;

  private static class Channel {
    short sampleValue;
    byte  stepIndex;
  }

  private static final Pool<Channel[]> POOL;
  static {
    final int initialCapacity = 8, max = 64;
    POOL = new Pool<Channel[]>(initialCapacity, max) {
      @Override
      protected Channel[] newObject() {
        final Channel[] channels = new Channel[CHANNELS];
        for (int i = 0; i < CHANNELS; i++) channels[i] = new Channel();
        return channels;
      }
    };
    POOL.fill(initialCapacity);
  }

  public static void decompress(ByteBuffer in, ByteBuffer out, int numChannels) {
    Channel[] channels = POOL.obtain();
    try {
      decompress(in, out, numChannels, channels);
    } finally {
      POOL.free(channels);
    }
  }

  public static void decompress(ByteBuffer in, ByteBuffer out, int numChannels, Channel[] state) {
    assert in.order() == ByteOrder.LITTLE_ENDIAN && out.order() == ByteOrder.LITTLE_ENDIAN : "in.order() = " + in.order() + "; out.order() = " + out.order();

    byte stepshift = (byte) (in.getShort() >>> Byte.SIZE);
    for (int i = 0; i < numChannels; i++) {
      Channel c = state[i];
      c.stepIndex = INITIAL_ADPCM_STEP_INDEX;
      c.sampleValue = in.getShort();
      out.putShort(c.sampleValue);
    }

    int current = 0;
    while (in.hasRemaining()) {
      byte op = in.get();
      Channel channel = state[current];
      if ((op & 0x80) != 0) {
        switch (op & 0x7F) {
          case 0: // write current value
            if (channel.stepIndex != 0) channel.stepIndex--;
            out.putShort(channel.sampleValue);
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

        out.putShort(channel.sampleValue);
        channel.stepIndex += CHANGE_TABLE[op & 0x1F];
        if (channel.stepIndex < 0) {
          channel.stepIndex = 0;
        } else if (channel.stepIndex >= STEP_TABLE.length) {
          channel.stepIndex = (byte) (STEP_TABLE.length - 1);
        }

        current = (current + 1) % numChannels;
      }
    }
  }
}
