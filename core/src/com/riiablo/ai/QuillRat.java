package com.riiablo.ai;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.entity.Entity;
import com.riiablo.entity.Missile;
import com.riiablo.entity.Monster;
import com.riiablo.entity.Player;

public class QuillRat extends AI {
  enum State implements com.badlogic.gdx.ai.fsm.State<Monster> {
    IDLE {
      @Override
      public void enter(Monster entity) {
        entity.setMode(Monster.MODE_NU);
      }
    },
    WANDER,
    APPROACH,
    ATTACK;

    @Override public void enter(Monster entity) {}
    @Override public void update(Monster entity) {}
    @Override public void exit(Monster entity) {}
    @Override
    public boolean onMessage(Monster entity, Telegram telegram) {
      return false;
    }
  }

  final StateMachine<Monster, State> stateMachine;
  float nextAction;
  float time;
  Missiles.Entry missile;

  public QuillRat(Monster entity) {
    super(entity);
    stateMachine = new DefaultStateMachine<>(entity, State.IDLE);
    monsound = "spikefiend";
    missile = Riiablo.files.Missiles.get(entity.monstats.MissA2);
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
      float melerng = 2f + entity.monstats2.MeleeRng;
      for (Entity ent : Riiablo.engine.newIterator()) {
        if (ent instanceof Player) {
          float dst = entity.position().dst(ent.position());
          if (dst < melerng) {
            entity.setPath(null, null);
            stateMachine.changeState(State.ATTACK);
            entity.sequence(Monster.MODE_A1, Monster.MODE_NU);
            Riiablo.audio.play(monsound + "_attack_1", true);
            time = MathUtils.random(1f, 2);
            return;
          } else if (dst < params[0]) {
            if (MathUtils.randomBoolean(params[1] / 100f)) {
              entity.setPath(null, null);
              entity.lookAt(ent);
              stateMachine.changeState(State.ATTACK);
              entity.sequence(Monster.MODE_A2, Monster.MODE_NU);
              Riiablo.audio.play(monsound + "_shoot_1", true);
              time = MathUtils.random(1f, 2);

              Missile miss = new Missile(missile);
              miss.position().set(entity.position());
              miss.lookAt(ent); // TODO: should be able to set angle to firing entity angle later
              Riiablo.engine.add(miss);
              return;
            }
          }
        }
      }
    }

    switch (stateMachine.getCurrentState()) {
      case IDLE:
        if (nextAction < 0) {
          entity.target().setZero();
          stateMachine.changeState(State.WANDER);
        }
        break;
      case WANDER:
        Vector2 target = entity.target();
        if (entity.position().epsilonEquals(target) && !entity.targets().hasNext()) {
          nextAction = MathUtils.random(0f, 1);
          stateMachine.changeState(State.IDLE);
        } else if (target.isZero()) {
          Vector2 dst = entity.position().cpy();
          dst.x += MathUtils.random(-5, 5);
          dst.y += MathUtils.random(-5, 5);
          entity.setPath(entity.map, dst);
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
