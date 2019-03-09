package com.riiablo.audio;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;

import java.util.Deque;
import java.util.LinkedList;

public class MusicController implements Music.OnCompletionListener {

  private static final String TAG = "MusicController";

  @NonNull
  private final AssetManager ASSETS;

  @NonNull
  private final Deque<String> PLAYLIST;

  @Nullable
  private Music track;

  @Nullable
  private String asset;

  public MusicController(@NonNull AssetManager assetManager) {
    this.ASSETS = Preconditions.checkNotNull(assetManager, "The AssetManager cannot be null");
    this.PLAYLIST = new LinkedList<>();
  }

  public void enqueue(String asset) {
    PLAYLIST.add(asset);
    if (!isPlaying()) {
      next();
    }
  }

  public void enqueue(AssetDescriptor<Music> asset) {
    enqueue(asset.fileName);
  }

  public void stop() {
    final Music track = this.track;
    if (track == null) {
      return;
    }

    // assert asset != null;
    if (ASSETS.isLoaded(asset)) ASSETS.unload(asset);
    track.dispose();
  }

  public boolean isPlaying() {
    return track != null && track.isPlaying();
  }

  public void play() {
    next();
  }

  public void play(@NonNull String asset) {
    Preconditions.checkNotNull(asset, "Asset cannot be null");
    PLAYLIST.addFirst(asset);
    next();
  }

  public void play(AssetDescriptor<Music> asset) {
    play(asset.fileName);
  }

  public void next() {
    stop();
    if (PLAYLIST.isEmpty()) {
      return;
    }

    this.asset = PLAYLIST.removeFirst();
    ASSETS.load(asset, Music.class);
    ASSETS.finishLoadingAsset(asset);
    this.track = ASSETS.get(asset, Music.class);
    track.setOnCompletionListener(this);
    track.play();
    if (Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
      Gdx.app.debug(TAG, "Now playing \"" + asset + "\"");
    }
  }


  @Override
  public void onCompletion(@NonNull Music music) {
    next();
  }
}
