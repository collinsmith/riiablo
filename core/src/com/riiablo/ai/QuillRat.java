package com.riiablo.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.entity.Monster;

public class QuillRat extends AI {

  public QuillRat(Monster entity) {
    super(entity);
  }

  @Override
  public void update(float delta) {
    Vector2 target = entity.target();
    if (target.isZero() || (entity.position().epsilonEquals(target) && !entity.targets().hasNext())) {
      Vector2 dst = entity.position().cpy();
      dst.x += MathUtils.random(-5, 5);
      dst.y += MathUtils.random(-5, 5);
      entity.setPath(entity.map, dst);
    }
  }

  @Override
  public String getState() {
    return "WANDER";
  }
}
