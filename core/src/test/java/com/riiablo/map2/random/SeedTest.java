package com.riiablo.map2.random;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.math.MathUtils;

class SeedTest {
  @RepeatedTest(10)
  void encode() {
    long seed0 = MathUtils.random.nextLong();
    long seed1 = MathUtils.random.nextLong();
    String encoding = Seed.encode(seed0, seed1);
    Seed seed = Seed.decode(encoding);
    assertEquals(seed0, seed.seed0);
    assertEquals(seed1, seed.seed1);
  }
}
