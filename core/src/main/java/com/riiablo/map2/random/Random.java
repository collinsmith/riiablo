package com.riiablo.map2.random;

import com.badlogic.gdx.math.RandomXS128;

public class Random extends RandomXS128 {
  public Seed seed() {
    long seed0 = getState(0);
    long seed1 = getState(1);
    return Seed.from(seed0, seed1);
  }

  public void seed(Seed seed) {
    super.setState(seed.seed0, seed.seed1);
  }
}
