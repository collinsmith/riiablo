package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.CofComponent;

import org.apache.commons.math3.util.FastMath;

public class AngularVelocitySystem extends IteratingSystem {
  private static final float ANGULAR_VELOCITY = MathUtils.PI * 8;

  private final ComponentMapper<AngleComponent> angleComponent = ComponentMapper.getFor(AngleComponent.class);

  public AngularVelocitySystem() {
    super(Family.all(AnimationComponent.class, CofComponent.class, AngleComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    AngleComponent angleComponent = this.angleComponent.get(entity);
    Vector2 angle  = angleComponent.angle;
    Vector2 target = angleComponent.target;
    if (angle.epsilonEquals(target)) {
      return;
    }

    float acos = (float) FastMath.acos(angle.dot(target));
    float asin = (float) FastMath.asin(angle.crs(target));

    float theta = ANGULAR_VELOCITY * delta;
    if (acos < theta) {
      angle.set(target);
      return;
    }

    theta = asin < 0 ? -theta : theta;
    angle.rotateRad(theta);
  }
}
