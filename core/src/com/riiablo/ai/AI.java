package com.riiablo.ai;

import com.riiablo.entity.Monster;
import com.riiablo.screen.GameScreen;

public abstract class AI {
  public static final AI IDLE = new Idle();

  protected Monster entity;

  public AI(Monster entity) {
    this.entity = entity;
  }

  public void interact(GameScreen gameScreen) {}

  public void update(float delta) {}
}
