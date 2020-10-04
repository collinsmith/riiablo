package com.riiablo.video;

import com.riiablo.io.ByteInput;
import com.riiablo.io.InvalidFormat;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class BinkAudio {
  private static final Logger log = LogManager.getLogger(BinkAudio.class);

  static final int SIZE = 0x0C;
  static final int MAX_CHANNELS = 2;

  static final int FLAG_AUDIO_16BITS = 0x4000;
  static final int FLAG_AUDIO_STEREO = 0x2000;
  static final int FLAG_AUDIO_DCT    = 0x1000;

  private static final int[] BANDS = {
      100, 200, 300, 400, 510, 630, 770, 920, 1080, 1270, 1480, 1720, 2000,
      2320, 2700, 3150, 3700, 4400, 5300, 6400, 7700, 9500, 12000, 15500,
      24500
  };

  private static final int[] RLE = {
      2, 3, 4, 5, 6, 8, 9, 10, 11, 12, 13, 14, 15, 16, 32, 64
  };

  final short numChannels;
  final int sampleRate;
  final int flags;
  final int id;

  final int frameLen;
  final int overlapLen;
  final int blockSize;
  final int halfSampleRate;

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
}
