package com.riiablo.ai;

import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.entity.Entity;
import com.riiablo.entity.Monster;
import com.riiablo.entity.Player;

public class Zombie extends AI {
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

  private final float SLEEP;

  int[] pa;
  final StateMachine<Monster, State> stateMachine;
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

    SLEEP = Animation.FRAME_DURATION * entity.monstats.aidel[0];
    stateMachine = new DefaultStateMachine<>(entity, State.IDLE);
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
      float melerng = 1.41f + entity.monstats2.MeleeRng;
      for (Entity ent : Riiablo.engine.newIterator()) {
        if (ent instanceof Player) {
          float dst = entity.position().dst(ent.position());
          if (dst < melerng) {
            entity.setPath(null, null);
            stateMachine.changeState(State.ATTACK);
            entity.sequence(MathUtils.randomBoolean(pa[3] / 100f) ? Monster.MODE_A2 : Monster.MODE_A1, Monster.MODE_NU);
            Riiablo.audio.play(entity.monstats.MonSound + "_attack_1", true);
            time = MathUtils.random(1f, 2);
            return;
          } else if (dst < pa[1]) {
            if (MathUtils.randomBoolean(pa[0] / 100f)) {
              entity.setPath(entity.map, ent.position());
              stateMachine.changeState(State.APPROACH);
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
          nextAction = MathUtils.random(3f, 5);
          stateMachine.changeState(State.IDLE);
        } else if (target.isZero()) {
          Vector2 dst = entity.position().cpy();
          dst.x += MathUtils.random(-5, 5);
          dst.y += MathUtils.random(-5, 5);
          entity.setPath(entity.map, dst);
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
}
