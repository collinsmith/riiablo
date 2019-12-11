package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Exclude;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.Riiablo;
import com.riiablo.engine.server.component.AnimData;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.Monster;
import com.riiablo.engine.server.component.MovementModes;
import com.riiablo.engine.server.component.Running;
import com.riiablo.engine.server.component.Sequence;
import com.riiablo.engine.server.component.Velocity;

@All({MovementModes.class, Velocity.class, AnimData.class, CofReference.class})
@Exclude(Sequence.class)
public class VelocityModeChanger extends IteratingSystem {
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<AnimData> mAnimData;
  protected ComponentMapper<Running> mRunning;
  protected ComponentMapper<MovementModes> mMovementModes;
  protected ComponentMapper<Monster> mMonster;

  protected CofManager cofs;

  @Override
  protected void begin() {
    Vector2 velocity = mVelocity.get(Riiablo.game.player).velocity;
    if (velocity.isZero()) return;
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
      mRunning.remove(Riiablo.game.player);
      velocity.setLength(6);
    } else {
      mRunning.create(Riiablo.game.player);
      velocity.setLength(9);
    }
  }

  /**
   * TODO: it would appear after testing that monsters may require a separate system to override
   *       their movement speed correctly from players. Need to investigate more when I can create
   *       and environment where I can adjust speeds to try and compare and see if I can refine
   *       my algorithm. Below method looks sufficient for now.
   */
  @Override
  protected void process(int entityId) {
    Velocity velocity = mVelocity.get(entityId);
    Vector2 currentVelocity = velocity.velocity;
    if (currentVelocity.isZero()) {
      cofs.setMode(entityId, mMovementModes.get(entityId).NU);
      mAnimData.get(entityId).override = -1;
    } else if (mMonster.has(entityId)) {
      AnimData animData = mAnimData.get(entityId);
      if (mRunning.has(entityId)) {
        cofs.setMode(entityId, mMovementModes.get(entityId).RN);
        animData.override = MathUtils.roundPositive(animData.speed * currentVelocity.len() / velocity.runSpeed);
      } else {
        cofs.setMode(entityId, mMovementModes.get(entityId).WL);
        animData.override = MathUtils.roundPositive(animData.speed * currentVelocity.len() / velocity.walkSpeed);
      }
    } else {
      if (mRunning.has(entityId)) {
        cofs.setMode(entityId, mMovementModes.get(entityId).RN);
        mAnimData.get(entityId).override = MathUtils.roundPositive(16 * currentVelocity.len());
      } else {
        cofs.setMode(entityId, mMovementModes.get(entityId).WL);
        mAnimData.get(entityId).override = MathUtils.roundPositive(32 * currentVelocity.len());
      }
    }
  }
}
