package com.riiablo.video;

import java.util.Arrays;
import org.apache.commons.math3.util.FastMath;

import com.riiablo.io.BitInput;
import com.riiablo.io.ByteInput;
import com.riiablo.io.InvalidFormat;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class BinkAudio {
  private static final Logger log = LogManager.getLogger(BinkAudio.class);

  static final int SIZE = 0x0C;
  static final int MAX_CHANNELS = 2;
  static final int BLOCK_MAX_SIZE = MAX_CHANNELS << 11;

  static final int FLAG_AUDIO_16BITS = 0x4000;
  static final int FLAG_AUDIO_STEREO = 0x2000;
  static final int FLAG_AUDIO_DCT    = 0x1000;

  private static final int[] CRIT_FREQ = {
      100, 200, 300, 400, 510, 630, 770, 920, 1080, 1270, 1480, 1720, 2000,
      2320, 2700, 3150, 3700, 4400, 5300, 6400, 7700, 9500, 12000, 15500,
      24500
  };

  private static final int[] RLE = {
      2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 32, 64
  };

  private final float[] QUANTS;
  private final int[] BANDS;
  private final float[][] PREVIOUS = new float[MAX_CHANNELS][BLOCK_MAX_SIZE >>> 4];

  final short numChannels;
  final int sampleRate;
  final int flags;
  final int id;

  final int frameLen;
  final int overlapLen;
  final int blockSize;
  final int halfSampleRate;
  final int numBands;
  final float root;

  boolean first;

  BinkAudio(ByteInput in) {
    log.trace("slicing {} bytes", SIZE);
    in = in.readSlice(SIZE);

    in.skipBytes(2); // unknown -- I've seen { 0x00, 0x1C }

    numChannels = in.readSafe16u();
    log.trace("numChannels: {}", numChannels);
    if (numChannels < 1 || numChannels > MAX_CHANNELS) {
      throw new InvalidFormat(in, "numChannels(" + numChannels + ") not in [" + 1 + ".." + MAX_CHANNELS + "]");
    }

    sampleRate = in.read16u();
    log.trace("sampleRate: {} Hz", sampleRate);

    flags = in.read16u();
    log.tracef("flags: 0x%04x (%s)", flags, getFlagsString());

    id = in.readSafe32u();
    log.trace("id: {}", id);

    final int frameLenBits;
    if (sampleRate < 22050) {
      frameLenBits = 9;
    } else if (sampleRate < 44100) {
      frameLenBits = 10;
    } else {
      frameLenBits = 11;
    }

    frameLen = 1 << frameLenBits;
    overlapLen = frameLen >> 4;
    blockSize = (frameLen - overlapLen) * numChannels;
    halfSampleRate = (sampleRate + 1) / 2;
    root = frameLen / ((float) FastMath.sqrt(frameLen) * 32768f);
    // if coded->id == RDFT then frameLen becomes 2f

    QUANTS = new float[96];
    for (int i = 0; i < 96; i++) {
      /* constant is result of 0.066399999/log10(M_E) */
      QUANTS[i] = (float) FastMath.exp(i * 0.15289164787221953823f) * root;
    }

    int numBands;
    for (numBands = 1; numBands < 25 && halfSampleRate > CRIT_FREQ[numBands - 1]; numBands++);
    this.numBands = numBands;

    BANDS = new int[numBands + 1];
    BANDS[0] = 2;
    for (int i = 1; i < numBands; i++) {
      BANDS[i] = (CRIT_FREQ[i - 1] * frameLen / halfSampleRate) & ~1;
    }
    BANDS[numBands] = frameLen;

    first = true;
  }

  public float[][] createOut() {
    return new float[MAX_CHANNELS][BLOCK_MAX_SIZE];
  }

  public String getFlagsString() {
    if (flags == 0) return "0";
    StringBuilder builder = new StringBuilder(64);
    if ((flags & FLAG_AUDIO_16BITS) == FLAG_AUDIO_16BITS) builder.append("16BITS|");
    if ((flags & FLAG_AUDIO_STEREO) == FLAG_AUDIO_STEREO) builder.append("STEREO|");
    if (builder.length() > 0) builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  public boolean isMono() {
    return (flags & FLAG_AUDIO_STEREO) == 0;
  }

  void decode(BitInput bits, float[][] out) {
    int ch, i, j, k;
    float q;
    float[] quant = new float[25];
    int width, coeff;
    for (ch = 0; ch < numChannels; ch++) {
      final float[] coeffs = out[ch];
      coeffs[0] = readFloat29(bits) * root;
      coeffs[1] = readFloat29(bits) * root;

      for (i = 0; i < numBands; i++) {
        final short value = bits.read8u();
        quant[i] = QUANTS[Math.min(value, 95)];
      }

      k = 0;
      q = quant[0];

      // parse coefficients
      i = 2;
      while (i < frameLen) {
        {
          int v = bits.read1();
          if (v != 0) {
            v = bits.read7u(4);
            j = i + RLE[v] << 3;
          } else {
            j = i + 8;
          }
        }

        j = Math.min(j, frameLen);

        width = bits.read7u(4);
        if (width == 0) {
          Arrays.fill(coeffs, i, coeffs.length, 0);
          i = j;
          while (BANDS[k] < i) {
            q = quant[k++];
          }
        } else {
          while (i < j) {
            if (BANDS[k] == i) {
              q = quant[k++];
            }
            coeff = bits.read31u(width);
            if (coeff != 0) {
              final int v = bits.read1();
              coeffs[i] = (v != 0 ? -q : q) * coeff;
            } else {
              coeffs[i] = 0f;
            }
            i++;
          }
        }
      }

      if (false) { // dct stuff
        coeffs[0] /= 0.5f;
        //dct calc stuff
      } else if (false) { // rdft decoder stuff

      }
    }

    for (ch = 0; ch < numChannels; ch++) {
      final float[] current = out[ch];
      final float[] previous = PREVIOUS[ch];
      int count = overlapLen * numChannels;
      if (!first) {
        j = ch;
        for (i = 0; i < overlapLen; i++, j += numChannels) {
          out[ch][i] = (previous[i] * (count - j) + current[i] * j) / count;
        }
      }
      System.arraycopy(previous, 0, current, frameLen - overlapLen, overlapLen);
    }

    first = false;
  }

  static float readFloat29(BitInput bits) {
    int power = bits.read32(5);
    float f = FastMath.scalb(bits.read32(23), power - 23);
    return bits.readBoolean() ? f : -f;
  }
}
