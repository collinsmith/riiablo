package com.riiablo.ai;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.Player;

public class Fallen extends AI {
  enum State implements com.badlogic.gdx.ai.fsm.State<Integer> {
    IDLE,
    WANDER,
    APPROACH,
    ATTACK;

    @Override public void enter(Integer entityId) {}
    @Override public void update(Integer entityId) {}
    @Override public void exit(Integer entityId) {}
    @Override public boolean onMessage(Integer entityId, Telegram telegram) {
      return false;
    }
  }

  protected ComponentMapper<Class> mClass;

  private static EntitySubscription enemyEntities;

  final Vector2 tmpVec2 = new Vector2();

  final StateMachine<Integer, State> stateMachine;
  float nextAction;
  float time;

  public Fallen(int entityId) {
    super(entityId);
    stateMachine = new DefaultStateMachine<>(entityId, State.IDLE);
  }

  @Override
  public void initialize() {
    super.initialize();
    if (enemyEntities == null) {
      enemyEntities = Riiablo.engine.getAspectSubscriptionManager().get(Aspect
              .all(Class.class)
              .one(Player.class));
    }
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
      Vector2 entityPos = mPosition.get(entityId).position;
      float melerng = 2f + monster.monstats2.MeleeRng;
      IntBag entities = enemyEntities.getEntities();
      for (int i = 0, size = entities.size(); i < size; i++) {
        int ent = entities.get(i);
        Class.Type type = mClass.get(ent).type;
        switch (type) {
          case PLR:
            Vector2 targetPos = mPosition.get(ent).position;
            float dst = entityPos.dst(targetPos);
            if (dst < melerng) {
              pathfinder.findPath(entityId, null);
              stateMachine.changeState(State.ATTACK);
              mSequence.create(entityId).sequence(MathUtils.randomBoolean(params[3] / 100f) ? Engine.Monster.MODE_A2 : Engine.Monster.MODE_A1, Engine.Monster.MODE_NU);
              Riiablo.audio.play(monsound + "_attack_1", true);
              time = MathUtils.random(1f, 2);
              return;
            } else if (dst < 25) {
              if (MathUtils.randomBoolean(params[0] / 100f)) {
                pathfinder.findPath(entityId, targetPos);
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
          pathfinder.findPath(entityId, null);
          stateMachine.changeState(State.WANDER);
        }
        break;
      case WANDER:
        if (!mPathfind.has(entityId)) {
          nextAction = MathUtils.random(0f, 1);
          stateMachine.changeState(State.IDLE);
        } else {
          Vector2 dst = tmpVec2.set(mPosition.get(entityId).position);
          dst.add(MathUtils.random(-5, 5), MathUtils.random(-5, 5));
          pathfinder.findPath(entityId, dst);
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
