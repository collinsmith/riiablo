package com.riiablo.engine;

import com.artemis.BaseSystem;

public abstract class IntervalBaseSystem extends BaseSystem {
  private final float interval;

  protected float acc;

  private float delta;

  public IntervalBaseSystem(float interval) {
    this.interval = interval;
  }

  protected float getIntervalDelta() {
    return interval + delta;
  }

  protected float getTimeDelta() {
    return world.getDelta();
  }

  @Override
  protected boolean checkProcessing() {
    acc += getTimeDelta();
    if (acc >= interval) {
      acc -= interval;
      delta = (acc - delta);
      return true;
    }

    return false;
  }
}
