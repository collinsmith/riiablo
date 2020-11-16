package com.riiablo.video;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

public class VideoPlayer implements Disposable {
  private List<AudioPacket> audioPackets;

  private BIK bik;
  private AudioDevice[] audio;
  private float[][][] out;
  private int frame;
  private float time, nextFrame;
  private ExecutorService audioStreams;
  // FIXME: child threads should throw an error to parent?

  public VideoPlayer() {
    
  }

  public void play(BIK bik) {
    if (this.bik != null) {
      throw new IllegalStateException("this.bik(" + this.bik + ") is not null");
    }
    this.bik = bik;

    audioPackets = new ArrayList<>(bik.numTracks);
    audioStreams = Executors.newFixedThreadPool(bik.numTracks, new ThreadFactory() {
      int i = 0;

      @Override
      public Thread newThread(Runnable r) {
        final Thread t = new Thread(r);
        t.setName("VideoPlayer-" + i++);
        t.setDaemon(true);
        return t;
      }
    });
    audio = new AudioDevice[bik.numTracks];
    out = new float[bik.numTracks][][];
    for (int i = 0, s = bik.numTracks; i < s; i++) {
      final BinkAudio track = bik.track(i);
      final AudioDevice device = audio[i] = Gdx.audio.newAudioDevice(track.sampleRate, track.isMono());
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
      bik.decode(frame++, audioPackets, audio[0], out[0]);
      for (AudioPacket audioPacket : audioPackets) {
        audioStreams.submit(audioPacket);
      }
    }
  }

  public void draw(Batch batch) {
  }

  @Override
  public void dispose() {
    audioStreams.shutdown();
    try {
      audioStreams.awaitTermination(5L, TimeUnit.SECONDS);
    } catch (InterruptedException t) {
      audioStreams.shutdownNow();
    }
  }
}
