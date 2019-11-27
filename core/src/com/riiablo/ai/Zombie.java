package com.riiablo.ai;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.riiablo.Riiablo;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.PathfindComponent;
import com.riiablo.engine.component.PlayerComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.SizeComponent;
import com.riiablo.engine.component.TypeComponent;
import com.riiablo.engine.component.VelocityComponent;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;
import com.riiablo.map.pfa.GraphPath;

import java.util.Iterator;

public class Zombie extends AI {
  enum State implements com.badlogic.gdx.ai.fsm.State<Entity> {
    IDLE,
    WANDER,
    APPROACH,
    ATTACK;

    @Override public void enter(Entity entity) {}
    @Override public void update(Entity entity) {}
    @Override public void exit(Entity entity) {}
    @Override public boolean onMessage(Entity entity, Telegram telegram) {
      return false;
    }
  }

  private static final ComponentMapper<MapComponent> mapComponent = ComponentMapper.getFor(MapComponent.class);
  private static final ComponentMapper<SizeComponent> sizeComponent = ComponentMapper.getFor(SizeComponent.class);
  private static final ComponentMapper<PathfindComponent> pathfindComponent = ComponentMapper.getFor(PathfindComponent.class);
  private static final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);

  private static final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private static final ComponentMapper<TypeComponent> typeComponent = ComponentMapper.getFor(TypeComponent.class);
  private static final Family enemyFamily = Family.all(TypeComponent.class).one(PlayerComponent.class).get();
  private static ImmutableArray<Entity> enemyEntities;

  final Vector2 tmpVec2 = new Vector2();

  final StateMachine<Entity, State> stateMachine;
  float nextAction;
  float time;

  public Zombie(Entity entity) {
    super(entity);
    stateMachine = new DefaultStateMachine<>(entity, State.IDLE);
    if (enemyEntities == null) enemyEntities = Riiablo.engine.getEntitiesFor(enemyFamily);
  }

  @Override
  public void update(float delta) {
    stateMachine.update();
    nextAction -= delta;
    time -= delta;
    if (time > 0) {
      return;
    }

    time = SLEEP;

    if (stateMachine.getCurrentState() != State.ATTACK) {
      Vector2 entityPos = positionComponent.get(entity).position;
      float melerng = 1.41f + monsterComponent.monstats2.MeleeRng;
      for (Entity ent : enemyEntities) {
        TypeComponent typeComponent = this.typeComponent.get(ent);
        switch (typeComponent.type) {
          case PLR:
            Vector2 targetPos = positionComponent.get(ent).position;
            float dst = entityPos.dst(targetPos);
            if (dst < melerng) {
              setPath(null);
              stateMachine.changeState(State.ATTACK);
              //entity.sequence(MathUtils.randomBoolean(params[3] / 100f) ? Monster.MODE_A2 : Monster.MODE_A1, Monster.MODE_NU);
              Riiablo.audio.play(monsound + "_attack_1", true);
              time = MathUtils.random(1f, 2);
              return;
            } else if (dst < params[1]) {
              if (MathUtils.randomBoolean(params[0] / 100f)) {
                setPath(targetPos);
                stateMachine.changeState(State.APPROACH);
                return;
              }
            }
            break;
        }
      }
    }

    switch (stateMachine.getCurrentState()) {
      case IDLE:
        if (nextAction < 0) {
          entity.remove(PathfindComponent.class);
          velocityComponent.get(entity).velocity.setZero();
          stateMachine.changeState(State.WANDER);
        }
        break;
      case WANDER:
        if (!pathfindComponent.has(entity)) {
          nextAction = MathUtils.random(3f, 5);
          stateMachine.changeState(State.IDLE);
        } else {
          Vector2 dst = tmpVec2.set(positionComponent.get(entity).position);
          dst.add(MathUtils.random(-5, 5), MathUtils.random(-5, 5));
          setPath(dst);
        }
        break;
      case APPROACH:
        nextAction = MathUtils.random(3f, 5);
        stateMachine.changeState(State.IDLE);
        break;
      case ATTACK:
        stateMachine.changeState(State.IDLE);
        break;
    }
  }

  private void setPath(Vector2 target) {
    if (target == null) {
      entity.remove(PathfindComponent.class);
      velocityComponent.get(entity).velocity.setZero();
      return;
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
    } else {
      Pools.free(path);
    }
  }

  @Override
  public String getState() {
    return stateMachine.getCurrentState().name();
  }
}
