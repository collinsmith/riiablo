package com.riiablo.ai;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.entity.Entity;
import com.riiablo.entity.Monster;
import com.riiablo.entity.Player;

public class Zombie extends AI {

  private static final float SLEEP = 1 / 25f;

  int[] pa;
  float nextAction;
  float time;

  public Zombie(Monster entity) {
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
    time += delta;
    if (time >= nextAction) {
      nextAction = time + SLEEP;
      for (Entity ent : Riiablo.engine.newIterator()) {
        if (ent instanceof Player && entity.position().dst(ent.position()) < pa[1]) {
          if (MathUtils.randomBoolean(pa[0] / 100f)) {
            entity.setPath(entity.map, ent.position());
            return;
          }
        }
      }

      Vector2 target = entity.target();
      if (target.isZero() || (entity.position().epsilonEquals(target) && !entity.targets().hasNext())) {
        Vector2 dst = entity.position().cpy();
        dst.x += MathUtils.random(-5, 5);
        dst.y += MathUtils.random(-5, 5);
        entity.setPath(entity.map, dst);
      }
    }
  }
}
