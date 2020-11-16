package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.riiablo.ai.AI;

@Transient
@PooledWeaver
public class AIWrapper extends PooledComponent {
  public AI ai = AI.IDLE;

  @Override
  protected void reset() {
    ai = AI.IDLE;
  }

  public AIWrapper findAI(int entityId, String ai) {
    this.ai = AI.findAI(entityId, ai);
    return this;
  }
}
