package com.riiablo.audio;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

import org.apache.commons.lang3.Validate;

import java.lang.ref.WeakReference;

public class VolumeControlledMusicLoader extends MusicLoader implements VolumeControlled<Music> {

  @Nullable
  private VolumeController<Music> controller;

  public VolumeControlledMusicLoader(@NonNull FileHandleResolver resolver,
                                     @NonNull VolumeController<Music> controller) {
    super(resolver);
    this.controller = Validate.notNull(controller, "VolumeController cannot be null");
  }

  @Override
  public void setVolumeController(@NonNull VolumeController<Music> controller) {
    this.controller = Validate.notNull(controller, "VolumeController cannot be null");
  }

  @Nullable
  @Override
  public VolumeController<Music> getVolumeController() {
    return controller;
  }

  @Override
  public void loadAsync(AssetManager manager, String fileName, FileHandle file,
                        MusicParameter parameter) {
    super.loadAsync(manager, fileName, file, parameter);
    final Music music = getLoadedMusic();
    if (controller != null) {
      music.setVolume(controller.getVolume());
      controller.manage(new WeakReference<>(music));
    }
  }
}
