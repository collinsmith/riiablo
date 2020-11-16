package com.riiablo.audio;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.badlogic.gdx.audio.Sound;

import java.lang.ref.WeakReference;

import com.riiablo.Cvars;
import com.riiablo.cvar.Cvar;
import com.riiablo.cvar.CvarStateAdapter;

public class SoundVolumeController implements VolumeController<Sound> {

  private static final String TAG = "MusicVolumeController";

  //private final Collection<WeakReference<Sounds>> MANAGED;

  private boolean soundEnabled;
  private boolean effectsEnabled;
  private float effectsVolume;

  public SoundVolumeController() {
    //this.MANAGED = new ArrayList<>();

    Cvars.Client.Sound.Enabled.addStateListener(new CvarStateAdapter<Boolean>() {
      @Override
      public void onChanged(@NonNull Cvar<Boolean> cvar,
                            @Nullable Boolean from, @Nullable Boolean to) {
        soundEnabled = to;
        refreshVolume();
      }
    });

    Cvars.Client.Sound.Effects.Enabled.addStateListener(new CvarStateAdapter<Boolean>() {
      @Override
      public void onChanged(@NonNull Cvar<Boolean> cvar, @Nullable Boolean from, @Nullable
          Boolean to) {
        effectsEnabled = to;
        refreshVolume();
      }
    });

    Cvars.Client.Sound.Effects.Volume.addStateListener(new CvarStateAdapter<Float>() {
      @Override
      public void onChanged(@NonNull Cvar<Float> cvar, @Nullable Float from, @Nullable Float to) {
        effectsVolume = to;
        refreshVolume();
      }
    });
  }

  // TODO: Add support for refreshing current effects' volumes
  @SuppressWarnings("EmptyMethod")
  private void refreshVolume() {
    //...
  }

  @Override
  public boolean isEnabled() {
    return effectsEnabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (effectsEnabled != enabled) {
      this.effectsEnabled = enabled;
      refreshVolume();
    }
  }

  @Override
  public float getVolume() {
    if (!soundEnabled || !effectsEnabled) {
      return 0.0f;
    }

    return effectsVolume;
  }

  @Override
  public void setVolume(float volume) {
    if (effectsVolume != volume) {
      this.effectsVolume = volume;
      refreshVolume();
    }
  }

  @Override
  public void manage(@NonNull WeakReference<Sound> sound) {
    //MANAGED.add(sound);
  }

  @Override
  public boolean remove(@Nullable Object obj) {
    //noinspection SuspiciousMethodCalls
    //return MANAGED.remove(obj);
    return false;
  }
}
