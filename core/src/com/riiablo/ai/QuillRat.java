package com.riiablo.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.entity.Monster;

public class QuillRat extends AI {

  int[] pa;

  public QuillRat(Monster entity) {
    super(entity);

    // TODO: difficulty-based params
    pa = new int[8];
    pa[0] = entity.monstats.aip1[0];
    pa[1] = entity.monstats.aip2[0];
    pa[2] = entity.monstats.aip3[0];
    pa[3] = entity.monstats.aip4[0];
    pa[4] = entity.monstats.aip5[0];
    pa[5] = entity.monstats.aip6[0];
    pa[6] = entity.monstats.aip7[0];
    pa[7] = entity.monstats.aip8[0];
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
}
