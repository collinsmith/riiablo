package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.riiablo.codec.Animation;
import com.riiablo.engine.Flags;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.MovementModeComponent;
import com.riiablo.engine.component.VelocityComponent;

public class MovementModeSystem extends IteratingSystem {
  private final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<MovementModeComponent> movementModeComponent = ComponentMapper.getFor(MovementModeComponent.class);

  public MovementModeSystem() {
    super(Family.all(VelocityComponent.class, CofComponent.class, MovementModeComponent.class).get());
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
      float velocity = velocityComponent.velocity.len();
      float fps;
      if ((entity.flags & Flags.RUNNING) == Flags.RUNNING) {
        fps = velocity * 5f;
        cofComponent.mode = movementModeComponent.RN; // each step = 2 yards
        //cofComponent.speed = 136;
      } else { // each step = 1 yards
        fps = velocity * 8f;
        cofComponent.mode = movementModeComponent.WL; // each step = 1 yards
        //cofComponent.speed = 136;
      }

      cofComponent.speed = MathUtils.roundPositive(256f / (fps / Animation.FRAMES_PER_SECOND));
      //System.out.println(velocity + " " + fps + " " + cofComponent.speed);
    }

  }
}
