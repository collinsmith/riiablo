package com.riiablo.ai;

import com.riiablo.engine.Engine;

public class Idle extends AI {
  public Idle() {
    super(Engine.INVALID_ENTITY);
  }

  @Override
  public void update(float delta) {}

  @Override
  public String getState() {
    return "IDLE";
  }
}
