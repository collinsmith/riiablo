package com.riiablo.ai;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.MonsterComponent;
import com.riiablo.engine.component.PathfindComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.SequenceComponent;
import com.riiablo.engine.component.SizeComponent;
import com.riiablo.engine.component.VelocityComponent;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;
import com.riiablo.map.pfa.GraphPath;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Constructor;
import java.util.Iterator;

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
  private static final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private static final ComponentMapper<AngleComponent> angleComponent = ComponentMapper.getFor(AngleComponent.class);
  private static final ComponentMapper<MapComponent> mapComponent = ComponentMapper.getFor(MapComponent.class);
  private static final ComponentMapper<SizeComponent> sizeComponent = ComponentMapper.getFor(SizeComponent.class);
  private static final ComponentMapper<PathfindComponent> pathfindComponent = ComponentMapper.getFor(PathfindComponent.class);
  private static final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);
  private static final ComponentMapper<SequenceComponent> sequenceComponent = ComponentMapper.getFor(SequenceComponent.class);
  private static final Vector2 tmpVec2 = new Vector2();

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

  protected AngleComponent lookAt(Entity target) {
    Vector2 targetPos = positionComponent.get(target).position;
    Vector2 entityPos = positionComponent.get(entity).position;
    tmpVec2.set(targetPos).sub(entityPos);
    AngleComponent angleComponent = this.angleComponent.get(entity);
    angleComponent.target.set(tmpVec2).nor();
    return angleComponent;
  }

  // FIXME: some actions must be too close to the border -- path finding seems to be tossing them
  protected PathfindComponent setPath(Vector2 target) {
    if (target == null) {
      entity.remove(PathfindComponent.class);
      velocityComponent.get(entity).velocity.setZero();
      return null;
    }

    PositionComponent positionComponent = this.positionComponent.get(entity);
    Vector2 position = positionComponent.position;

    int flags = DT1.Tile.FLAG_BLOCK_WALK;

    SizeComponent sizeComponent = this.sizeComponent.get(entity);
    int size = sizeComponent.size;

    MapComponent mapComponent = this.mapComponent.get(entity);
    Map map = mapComponent.map;

    GraphPath path = Pools.obtain(GraphPath.class);
    boolean success = map.findPath(position, target, flags, size, path);
    if (success) {
      map.smoothPath(flags, size, path);
      PathfindComponent pathfindComponent = com.riiablo.engine.Engine
          .getOrCreateComponent(entity, Riiablo.engine, PathfindComponent.class, this.pathfindComponent);
      pathfindComponent.path = path;
      pathfindComponent.targets = path.vectorIterator();
      pathfindComponent.targets.next(); // consume src position
      Iterator<Vector2> targets = pathfindComponent.targets;
      if (targets.hasNext()) {
        pathfindComponent.target.set(targets.next());
      } else {
        pathfindComponent.target.set(position);
      }
      return pathfindComponent;
    } else {
      Pools.free(path);
    }

    return null;
  }

  protected SequenceComponent sequence(int mode1, int mode2) {
    SequenceComponent sequenceComponent = Riiablo.engine.getOrCreateComponent(entity, SequenceComponent.class, this.sequenceComponent);
    sequenceComponent.mode1 = mode1;
    sequenceComponent.mode2 = mode2;
    return sequenceComponent;
  }

  protected Entity fire(Missiles.Entry missile) {
    PositionComponent positionComponent = this.positionComponent.get(entity);
    AngleComponent angleComponent = this.angleComponent.get(entity);
    Entity missileEntity = Riiablo.engine.createMissile(missile, angleComponent.target, positionComponent.position);
    Riiablo.engine.addEntity(missileEntity);
    return missileEntity;
  }
}
