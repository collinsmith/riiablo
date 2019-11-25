package com.riiablo.ai;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.riiablo.codec.Animation;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.MonsterComponent;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Constructor;

public abstract class AI implements InteractableComponent.Interactor {
  private static final String TAG = "AI";
  public static final AI IDLE = new Idle();

  public static AI findAI(Entity entity, MonsterComponent monsterComponent) {
    try {
      assert entity.getComponent(MonsterComponent.class) == monsterComponent;
      String ai = monsterComponent.monstats.AI;
      Class clazz = Class.forName("com.riiablo.ai." + ai);
      if (clazz == Idle.class) return AI.IDLE;
      Constructor constructor = clazz.getConstructor(Entity.class);
      return (AI) constructor.newInstance(entity);
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
      return AI.IDLE;
    }
  }

  private static final ComponentMapper<MonsterComponent> monsterComponents = ComponentMapper.getFor(MonsterComponent.class);

  protected final float SLEEP;
  protected final int[] params;
  protected Entity entity;
  protected MonsterComponent monsterComponent;
  protected String monsound;

  public AI(Entity entity) {
    this.entity = entity;
    // Special case for Idle AI -- TODO: fix Idle AI to remove this special case
    if (entity == null) {
      SLEEP = Float.POSITIVE_INFINITY;
      params = ArrayUtils.EMPTY_INT_ARRAY;
      return;
    }

    monsterComponent = monsterComponents.get(entity);
    MonStats.Entry monstats = monsterComponent.monstats;

    // TODO: difficulty-based params
    params = new int[8];
    params[0] = monstats.aip1[0];
    params[1] = monstats.aip2[0];
    params[2] = monstats.aip3[0];
    params[3] = monstats.aip4[0];
    params[4] = monstats.aip5[0];
    params[5] = monstats.aip6[0];
    params[6] = monstats.aip7[0];
    params[7] = monstats.aip8[0];

    SLEEP = Animation.FRAME_DURATION * monstats.aidel[0];
    monsound = monstats.MonSound;
  }

  @Override
  public void interact(Entity src, Entity entity) {}

  public void update(float delta) {}

  public String getState() {
    return "";
  }
}
