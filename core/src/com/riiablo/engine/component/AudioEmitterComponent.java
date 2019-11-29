package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.audio.Audio;

public class AudioEmitterComponent implements Component, Pool.Poolable {
  public Audio.Instance sound;
  public Interpolation interpolator = Interpolation.linear;

  @Override
  public void reset() {
    if (sound != null) sound.stop();
    sound = null;
    interpolator = Interpolation.linear;
  }
}
