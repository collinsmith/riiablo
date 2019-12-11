package com.riiablo.engine.client.component;

import com.artemis.PooledComponent;
import com.artemis.annotations.PooledWeaver;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

@Transient
@PooledWeaver
public class Label extends PooledComponent {
  public Actor actor;
  public final Vector2 offset = new Vector2();

  @Override
  protected void reset() {
    actor = null;
    offset.setZero();
  }
}
