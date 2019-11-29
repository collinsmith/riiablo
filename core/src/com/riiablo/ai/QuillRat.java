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
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.engine.Engine;
import com.riiablo.engine.component.PathfindComponent;
import com.riiablo.engine.component.PlayerComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.TypeComponent;
import com.riiablo.engine.component.VelocityComponent;

public class QuillRat extends AI {
  enum State implements com.badlogic.gdx.ai.fsm.State<Entity> {
    IDLE,
    WANDER,
    APPROACH,
    ATTACK;

    @Override public void enter(Entity entity) {}
    @Override public void update(Entity entity) {}
    @Override public void exit(Entity entity) {}
    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
      return false;
    }
  }

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
  Missiles.Entry missile;

  public QuillRat(Entity entity) {
    super(entity);
    stateMachine = new DefaultStateMachine<>(entity, State.IDLE);
    if (enemyEntities == null) enemyEntities = Riiablo.engine.getEntitiesFor(enemyFamily);
    monsound = "spikefiend";
    missile = Riiablo.files.Missiles.get(monsterComponent.monstats.MissA2);
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
      float melerng = 2f + monsterComponent.monstats2.MeleeRng;
      for (Entity ent : enemyEntities) {
        TypeComponent typeComponent = this.typeComponent.get(ent);
        switch (typeComponent.type) {
          case PLR:
            Vector2 targetPos = positionComponent.get(ent).position;
            float dst = entityPos.dst(targetPos);
            if (dst < melerng) {
              setPath(null);
              lookAt(ent);
              stateMachine.changeState(State.ATTACK);
              sequence(Engine.Monster.MODE_A1, Engine.Monster.MODE_NU);
              Riiablo.audio.play(monsound + "_attack_1", true);
              time = MathUtils.random(1f, 2);
              return;
            } else if (dst < params[0]) {
              if (MathUtils.randomBoolean(params[1] / 100f)) {
                setPath(null);
                lookAt(ent);
                stateMachine.changeState(State.ATTACK);
                sequence(Engine.Monster.MODE_A2, Engine.Monster.MODE_NU);
                Riiablo.audio.play(monsound + "_shoot_1", true);
                time = MathUtils.random(1f, 2);
                fire(missile);
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
        nextAction = MathUtils.random(0f, 1);
        stateMachine.changeState(State.IDLE);
        break;
      case ATTACK:
        stateMachine.changeState(State.IDLE);
        break;
    }
  }

  @Override
  public String getState() {
    return stateMachine.getCurrentState().name();
  }
}
