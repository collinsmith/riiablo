package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.engine.Flags;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.MovementModeComponent;
import com.riiablo.engine.component.SequenceComponent;
import com.riiablo.engine.component.VelocityComponent;

public class MovementModeSystem extends IteratingSystem {
  private final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<MovementModeComponent> movementModeComponent = ComponentMapper.getFor(MovementModeComponent.class);

  public MovementModeSystem() {
    super(Family.all(VelocityComponent.class, CofComponent.class, MovementModeComponent.class).exclude(SequenceComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    CofComponent cofComponent = this.cofComponent.get(entity);
    VelocityComponent velocityComponent = this.velocityComponent.get(entity);
    MovementModeComponent movementModeComponent = this.movementModeComponent.get(entity);
    if (velocityComponent.velocity.isZero()) {
      cofComponent.mode = movementModeComponent.NU;
      cofComponent.speed = CofComponent.SPEED_NULL;
    } else {
      //float velocity = velocityComponent.velocity.len();
      //float fps;
      if ((entity.flags & Flags.RUNNING) == Flags.RUNNING) {
        //fps = velocity * 5f;
        cofComponent.mode = movementModeComponent.RN; // each step = 2 yards
        cofComponent.speed = 136;
      } else { // each step = 1 yards
        //fps = velocity * 8f;
        cofComponent.mode = movementModeComponent.WL; // each step = 1 yards
        cofComponent.speed = 136;
      }

      /**
       * FIXME: below looks really close, still haven't figured it out though
       *        I've tried calculating some proportion of velocity -- still a possibility
       *               ... assuming each frame is 1/25 fps, so 12.5 frames would be 128 delta
       *        best results so far seem to just be using 128 for everything -- but this doesn't
       *           account for changes to velocity (faster movement should have faster animations)
       *           it's 136 now because that looked a bit better than 128, but more testing is
       *           needed once velocity can be modded past default (items, auras, etc)
       */
      //cofComponent.speed = MathUtils.roundPositive((cofComponent.cof.getNumFramesPerDir() / Animation.FRAMES_PER_SECOND) * 256f);
      //System.out.println(velocity + " " + fps + " " + cofComponent.speed);
    }

  }
}
