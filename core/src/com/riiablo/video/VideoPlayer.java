package com.riiablo.video;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

public class VideoPlayer implements Disposable {
  private BIK bik;
  private AudioDevice[] audio;
  private float[][][] out;
  private int frame;
  private float time, nextFrame;

  public VideoPlayer() {
    
  }

  public void play(BIK bik) {
    if (this.bik != null) {
      throw new IllegalStateException("this.bik(" + this.bik + ") is not null");
    }
    this.bik = bik;

    audio = new AudioDevice[bik.numTracks];
    out = new float[bik.numTracks][][];
    for (int i = 0, s = bik.numTracks; i < s; i++) {
      final BinkAudio track = bik.track(i);
//      final AudioDevice device = audio[i] = Gdx.audio.newAudioDevice(track.sampleRate, track.isMono());
      final AudioDevice device = audio[i] = Gdx.audio.newAudioDevice(track.sampleRate, true);
      device.setVolume(0.10f);
      out[i] = track.createOut();
    }
  }

  public void resize(int width, int height) {

  }

  public void update(float delta) {
    time += delta;
    if (time > nextFrame) {
      nextFrame += bik.delta;
      bik.decode(frame++, audio, out[0]);
    }
  }

  public void draw(Batch batch) {
  }

  @Override
  public void dispose() {

  }
}
