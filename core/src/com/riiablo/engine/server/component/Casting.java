package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;

import com.badlogic.gdx.math.Vector2;

@PooledWeaver
public class Casting extends PooledComponent {
  public final Vector2 target = new Vector2();
  public int skillId = -1;

  public Casting set(int skillId, Vector2 target) {
    this.skillId = skillId;
    this.target.set(target);
    return this;
  }

  @Override
  protected void reset() {
    skillId = -1;
    target.setZero();
  }
}
