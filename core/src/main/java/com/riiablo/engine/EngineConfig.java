package com.riiablo.engine;

public class EngineConfig {
  final int seed;
  final int diff;

  public EngineConfig(int seed, int diff) {
    this.seed = seed;
    this.diff = diff;
  }

  public int seed() {
    return seed;
  }

  public int diff() {
    return diff;
  }
}
