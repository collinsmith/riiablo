package com.riiablo.ai;

import com.artemis.ComponentMapper;
import com.artemis.annotations.EntityId;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.codec.Animation;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.client.ClientEntityFactory;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.Pathfinder;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Interactable;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.engine.server.component.Monster;
import com.riiablo.engine.server.component.PathWrapper;
import com.riiablo.engine.server.component.Pathfind;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Sequence;
import com.riiablo.engine.server.component.Size;
import com.riiablo.engine.server.component.Velocity;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Constructor;

public abstract class AI implements Interactable.Interactor {
  private static final String TAG = "AI";
  public static final AI IDLE = new Idle();

  public static AI findAI(int entityId, String ai) {
    try {
      Class clazz = Class.forName("com.riiablo.ai." + ai);
      if (clazz == Idle.class) return AI.IDLE;
      Constructor constructor = clazz.getConstructor(int.class);
      return (AI) constructor.newInstance(entityId);
    } catch (Throwable t) {
      Gdx.app.error(TAG, t.getMessage(), t);
      return AI.IDLE;
    }
  }

  protected ComponentMapper<Monster> mMonster;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<MapWrapper> mMapWrapper;
  protected ComponentMapper<Size> mSize;
  protected ComponentMapper<Pathfind> mPathfind;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Sequence> mSequence;
  protected ComponentMapper<Interactable> mInteractable;
  protected ComponentMapper<PathWrapper> mPathWrapper;

  protected CofManager cofs;
  protected Pathfinder pathfinder;
  protected ClientEntityFactory factory;

  private static final Vector2 tmpVec2 = new Vector2();

  protected float SLEEP = Float.POSITIVE_INFINITY;
  protected int[] params = ArrayUtils.EMPTY_INT_ARRAY;

  @EntityId
  protected int entityId;
  protected Monster monster;
  protected String monsound;

  public AI(int entityId) {
    this.entityId = entityId;
  }

  public void initialize() {
    if (this == IDLE) return;
    monster = mMonster.get(entityId);
    MonStats.Entry monstats = monster.monstats;

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
  public void interact(int src, int entityId) {}

  public void update(float delta) {}

  public String getState() {
    return "";
  }

  protected Angle lookAt(int target) {
    Vector2 targetPos = mPosition.get(target).position;
    Vector2 entityPos = mPosition.get(entityId).position;
    tmpVec2.set(targetPos).sub(entityPos);
    Angle angle = mAngle.get(entityId);
    angle.target.set(tmpVec2).nor();
    return angle;
  }

  protected int fire(Missiles.Entry missile) {
    Vector2 position = mPosition.get(entityId).position;
    Vector2 angle = mAngle.get(entityId).target;
    int missileId = factory.createMissile(missile, angle, position);
    return missileId;
  }
}
