package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.math.Interpolation;
import com.riiablo.audio.Audio;

@Transient
@PooledWeaver
public class SoundEmitter extends PooledComponent {
  public Audio.Instance sound;
  public Interpolation interpolator = Interpolation.linear;

  @Override
  protected void reset() {
    if (sound != null) sound.stop();
    sound = null;
    interpolator = Interpolation.linear;
  }

  public SoundEmitter set(Audio.Instance sound, Interpolation interpolator) {
    this.sound = sound;
    this.interpolator = interpolator;
    return this;
  }
}
