package com.riiablo.engine.server.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.Direction;

@PooledWeaver
public class Angle extends PooledComponent {
  private static final float DEFAULT_ANGLE = Direction.direction8ToRadians(Direction.DOWN);
  public final Vector2 angle  = Vector2.X.cpy().rotateRad(DEFAULT_ANGLE);
  public final Vector2 target = angle.cpy();

  @Override
  public void reset() {
    angle.rotateRad(DEFAULT_ANGLE);
    target.set(angle);
  }

  public Angle set(Vector2 angle) {
    this.angle.set(angle);
    this.target.set(angle);
    return this;
  }
}
