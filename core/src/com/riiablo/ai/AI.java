package com.riiablo.ai;

import com.riiablo.codec.Animation;
import com.riiablo.entity.Monster;
import com.riiablo.screen.GameScreen;

import org.apache.commons.lang3.ArrayUtils;

public abstract class AI {
  public static final AI IDLE = new Idle();

  protected final float SLEEP;
  protected final int[] params;
  protected Monster entity;
  protected String monsound;

  public AI(Monster entity) {
    this.entity = entity;

    // Special case for Idle AI -- TODO: fix Idle AI to remove this special case
    if (entity == null) {
      SLEEP = Float.POSITIVE_INFINITY;
      params = ArrayUtils.EMPTY_INT_ARRAY;
      return;
    }

    // TODO: difficulty-based params
    params = new int[8];
    params[0] = entity.monstats.aip1[0];
    params[1] = entity.monstats.aip2[0];
    params[2] = entity.monstats.aip3[0];
    params[3] = entity.monstats.aip4[0];
    params[4] = entity.monstats.aip5[0];
    params[5] = entity.monstats.aip6[0];
    params[6] = entity.monstats.aip7[0];
    params[7] = entity.monstats.aip8[0];

    SLEEP = Animation.FRAME_DURATION * entity.monstats.aidel[0];
    monsound = entity.monstats.MonSound;
  }

  public void interact(GameScreen gameScreen) {}

  public void update(float delta) {}

  public String getState() {
    return "";
  }
}
