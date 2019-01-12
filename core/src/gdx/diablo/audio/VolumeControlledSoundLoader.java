package gdx.diablo.audio;

import com.google.common.base.Preconditions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.lang.ref.WeakReference;

public class VolumeControlledSoundLoader extends SoundLoader implements VolumeControlled<Sound> {

  @Nullable
  private VolumeController<Sound> controller;

  @Nullable
  private Sound sound;

  public VolumeControlledSoundLoader(@NonNull FileHandleResolver resolver,
                                     @NonNull VolumeController<Sound> controller) {
    super(resolver);
    this.controller = Preconditions.checkNotNull(controller, "VolumeController cannot be null");
  }

  @Override
  public void setVolumeController(@NonNull VolumeController<Sound> controller) {
    this.controller = Preconditions.checkNotNull(controller, "VolumeController cannot be null");
  }

  @Nullable
  @Override
  public VolumeController<Sound> getVolumeController() {
    return controller;
  }

  @Override
  public void loadAsync(AssetManager manager, String fileName, FileHandle file,
                        SoundParameter parameter) {
    super.loadAsync(manager, fileName, file, parameter);
    final Sound sound = wrap(getLoadedSound());
    if (controller != null) {
      controller.manage(new WeakReference<>(sound));
    }
  }

  @Override
  public Sound loadSync(AssetManager manager, String fileName, FileHandle file, SoundParameter parameter) {
    final Sound sound = super.loadSync(manager, fileName, file, parameter);
    return wrap(sound);
  }

  private VolumeManagedSound wrap(@NonNull Sound sound) {
    return new VolumeManagedSound(sound);
  }

  private class VolumeManagedSound implements Sound {

    private final Sound PARENT;

    private VolumeManagedSound(@NonNull Sound sound) {
      this.PARENT = Preconditions.checkNotNull(sound, "Sounds file cannot be null");
    }

    @Override
    public long play() {
      return play(1.0f);
    }

    @Override
    public long play(float volume) {
      return play(volume, 1.0f, 0.0f);
    }

    @Override
    public long play(float volume, float pitch, float pan) {
      if (controller != null) {
        volume *= controller.getVolume();
      }

      return PARENT.play(volume, pitch, pan);
    }

    @Override
    public long loop() {
      return loop(1.0f);
    }

    @Override
    public long loop(float volume) {
      return loop(volume, 1.0f, 0.0f);
    }

    @Override
    public long loop(float volume, float pitch, float pan) {
      if (controller != null) {
        volume *= controller.getVolume();
      }

      return PARENT.loop(volume, pitch, pan);
    }

    @Override
    public void stop() {
      PARENT.stop();
    }

    @Override
    public void stop(long soundId) {
      PARENT.stop(soundId);
    }

    @Override
    public void pause() {
      PARENT.pause();
    }

    @Override
    public void pause(long soundId) {
      PARENT.pause(soundId);
    }

    @Override
    public void resume() {
      PARENT.resume();
    }

    @Override
    public void resume(long soundId) {
      PARENT.resume();
    }

    @Override
    public void dispose() {
      PARENT.dispose();
    }

    @Override
    public void setLooping(long soundId, boolean looping) {
      PARENT.setLooping(soundId, looping);
    }

    @Override
    public void setPitch(long soundId, float pitch) {
      PARENT.setPitch(soundId, pitch);
    }

    @Override
    public void setVolume(long soundId, float volume) {
      if (controller != null) {
        volume *= controller.getVolume();
      }

      PARENT.setVolume(soundId, volume);
    }

    @Override
    public void setPan(long soundId, float pan, float volume) {
      if (controller != null) {
        volume *= controller.getVolume();
      }

      PARENT.setPan(soundId, pan, volume);
    }
  }
}
