package com.riiablo.engine.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool;
import com.riiablo.ai.AI;

public class AIComponent implements Component, Pool.Poolable {
  public AI ai = AI.IDLE;

  @Override
  public void reset() {
    ai = AI.IDLE;
  }
}
