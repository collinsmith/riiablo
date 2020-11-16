package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.math.Vector2;

@PooledWeaver
public class Velocity extends PooledComponent {
  public final Vector2 velocity = new Vector2();
  public float walkSpeed;
  public float runSpeed;

  @Override
  protected void reset() {
    velocity.setZero();
    walkSpeed = 0;
    runSpeed = 0;
  }

  public Velocity set(float walkSpeed, float runSpeed) {
    this.walkSpeed = walkSpeed;
    this.runSpeed = runSpeed;
    return this;
  }
}
