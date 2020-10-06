package com.riiablo.video;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.io.FileUtils;

import com.badlogic.gdx.audio.AudioDevice;

import com.riiablo.io.BitUtils;
import com.riiablo.io.ByteInput;
import com.riiablo.io.InvalidFormat;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;
import com.riiablo.logger.MDC;

public class BIK {
  private static final Logger log = LogManager.getLogger(BIK.class);

  static final int EXTRADATA_SIZE = 1;
  static final int MAX_WIDTH = 7680;
  static final int MAX_HEIGHT = 4800;
  static final int MAX_TRACKS = 256;
  static final int SMUSH_BLOCK_SIZE = 512;

  private static final byte[] SIGNATURE = new byte[] { 0x42, 0x49, 0x4B };

  private static final int FLAG_VIDEO_ALPHA     = 0x00100000;
  private static final int FLAG_VIDEO_GRAYSCALE = 0x00020000;

  final ByteBuf buffer;
  final short version;
  final int size;
  final int numFrames;
  final int largestFrame;
  final int width;
  final int height;
  final float fps;
  final int flags;
  final int numTracks;
  final BinkAudio[] tracks;
  final int[] offsets;
  final BinkVideo video;

  public static BIK loadFromByteBuf(ByteBuf buffer) {
    return new BIK(buffer);
  }

  BIK(ByteBuf buffer) {
    log.trace("Reading bik...");
    this.buffer = buffer;

    ByteInput in = ByteInput.wrap(buffer);

    log.trace("Validating bik signature");
    in.readSignature(SIGNATURE);

    version = in.read8u();
    log.tracef("version: %c (0x%1$02x)", version);
    if (version != 0x69) {
      throw new InvalidFormat(in, "version(" + String.format("0x%02x", version) + ") is not supported!");
    }

    size = in.readSafe32u() + 8; // include {SIGNATURE, version, size}
    log.trace("size: {} ({} bytes)", FileUtils.byteCountToDisplaySize(size), size);

    numFrames = in.readSafe32u();
    log.trace("numFrames: {}", numFrames);

    largestFrame = in.readSafe32u();
    log.trace("largestFrame: {} bytes", largestFrame);

    in.skipBytes(4); // skip duplicate of numFrames

    width = in.readSafe32u();
    log.trace("width: {}", width);
    if (width < 1 || width > MAX_WIDTH) {
      throw new InvalidFormat(in, "width(" + width + ") not in [" + 1 + ".." + MAX_WIDTH + "]");
    }

    height = in.readSafe32u();
    log.trace("height: {}", height);
    if (height < 1 || height > MAX_HEIGHT) {
      throw new InvalidFormat(in, "height(" + height + ") not in [" + 1 + ".." + MAX_HEIGHT + "]");
    }

    final int fpsDividend = in.readSafe32u();
    log.trace("fpsDividend: {}", fpsDividend);
    if (fpsDividend <= 0) {
      throw new InvalidFormat(in, "fpsDividend(" + fpsDividend + ") <= 0");
    }

    final int fpsDivisor = in.readSafe32u();
    log.trace("fpsDivisor: {}", fpsDivisor);
    if (fpsDivisor <= 0) {
      throw new InvalidFormat(in, "fpsDivisor(" + fpsDivisor + ") <= 0");
    }

    fps = (float) fpsDividend / fpsDivisor;
    log.trace("fps: {} fps", fps);

    flags = in.read32();
    if (log.traceEnabled()) log.tracef("flags: 0x%08x (%s)", flags, getFlagsString());

    numTracks = in.readSafe32u();
    log.trace("numTracks: {}", numTracks);
    if (numTracks < 0 || numTracks > MAX_TRACKS) {
      throw new InvalidFormat(in, "numTracks(" + numTracks + ") not in [" + 0 + ".." + MAX_TRACKS + "]");
    }

    tracks = new BinkAudio[numTracks];
    for (int i = 0, s = numTracks; i < s; i++) {
      try {
        MDC.put("track", i);
        tracks[i] = new BinkAudio(in);
      } finally {
        MDC.remove("track");
      }
    }

    video = new BinkVideo();

    offsets = BitUtils.readSafe32u(in, numFrames + 1);
    for (int i = 0; i <= numFrames; i++) {
      boolean keyframe = (offsets[i] & 1) == 1;
      if (keyframe) {
        offsets[i] &= ~1;
      }
    }

    if (log.traceEnabled()) {
      StringBuilder builder = new StringBuilder(256);
      builder.append('[');
      for (int offset : offsets) {
        builder.append(Integer.toHexString(offset)).append(',');
      }
      builder.setCharAt(builder.length() - 1, ']');
      log.trace("offsets: {}", builder);
    }
  }

  public String getFlagsString() {
    if (flags == 0) return "0";
    StringBuilder builder = new StringBuilder(64);
    if ((flags & FLAG_VIDEO_ALPHA) == FLAG_VIDEO_ALPHA) builder.append("ALPHA|");
    if ((flags & FLAG_VIDEO_GRAYSCALE) == FLAG_VIDEO_GRAYSCALE) builder.append("GRAYSCALE|");
    if (builder.length() > 0) builder.setLength(builder.length() - 1);
    return builder.toString();
  }

  BinkAudio track(int track) {
    return tracks[track];
  }

  int numTracks() {
    return numTracks;
  }

  void decode(int frame, AudioDevice[] audio, float[][] out) {
    final int offset = offsets[frame];
    log.tracef("offset: +%x", offset);
    final ByteBuf slice = buffer.slice(offset, offsets[frame + 1] - offset);
    final ByteInput in = ByteInput.wrap(slice);

    for (int i = 0, s = numTracks; i < s; i++) {
      try {
        MDC.put("track", i);
        final int packetSize = in.readSafe32u();
        log.trace("packetSize: {} bytes", packetSize);

        final ByteInput audioPacket = in.readSlice(packetSize);
        System.out.println(ByteBufUtil.prettyHexDump(audioPacket.buffer()));
        final int numSamples = audioPacket.readSafe32u();
        log.trace("numSamples: {}", numSamples);

//        BinkAudio track = tracks[i];
//        track.decode(audioPacket.unalign(), out);
//        audio[i].writeSamples(out[0], 0, numSamples);

        log.trace("bytesRemaining: {} bytes", audioPacket.bytesRemaining());
      } finally {
        MDC.remove("track");
      }
    }

    final int packetSize = in.bytesRemaining();
    log.trace("packetSize: {} bytes", packetSize);
    final ByteInput videoPacket = in;
    System.out.println(ByteBufUtil.prettyHexDump(videoPacket.buffer()));
  }
}
