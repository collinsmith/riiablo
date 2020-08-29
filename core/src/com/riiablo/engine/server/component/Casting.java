package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.EntityId;
import com.artemis.annotations.PooledWeaver;

import com.badlogic.gdx.math.Vector2;

import com.riiablo.engine.Engine;

@PooledWeaver
public class Casting extends PooledComponent {
  public int skillId = -1;
  @EntityId
  public int targetId;
  public final Vector2 targetVec = new Vector2();

  public Casting set(int skillId, int targetId, Vector2 targetVec) {
    this.skillId = skillId;
    this.targetId = targetId;
    this.targetVec.set(targetVec);
    return this;
  }

  @Override
  protected void reset() {
    skillId = -1;
    targetId = Engine.INVALID_ENTITY;
    targetVec.setZero();
  }
}
