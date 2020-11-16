package com.riiablo.video;

import com.badlogic.gdx.audio.AudioDevice;

import com.riiablo.io.BitInput;
import com.riiablo.io.ByteInput;
import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

class AudioPacket implements Runnable {
  private static final Logger log = LogManager.getLogger(AudioPacket.class);

  final BinkAudio track;
  final BitInput bits;
  final int numSamples;
  final AudioDevice device;
  final float[][] out;

  public AudioPacket(BinkAudio track, ByteInput in, AudioDevice device, float[][] out) {
    this.track = track;
    this.numSamples = in.readSafe32u();
    this.bits = in.unalign();
    this.device = device;
    this.out = out;

    log.trace("numSamples: {}", numSamples);
    track.decode(bits, out);
  }

  @Override
  public void run() {
    device.writeSamples(out[0], 0, numSamples);
  }
}
