package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.server.component.Angle;

import org.apache.commons.math3.util.FastMath;

@All(Angle.class)
public class AngularVelocity extends IteratingSystem {
  private static final float ANGULAR_VELOCITY = MathUtils.PI * 8;

  protected ComponentMapper<Angle> mAngle;

  @Override
  protected void process(int entityId) {
    Angle angleComponent = mAngle.get(entityId);
    Vector2 angle  = angleComponent.angle;
    Vector2 target = angleComponent.target;
    if (angle.epsilonEquals(target)) {
      return;
    }

    float acos = (float) FastMath.acos(angle.dot(target));
    float asin = (float) FastMath.asin(angle.crs(target));

    float theta = ANGULAR_VELOCITY * world.delta;
    if (acos < theta) {
      angle.set(target);
      return;
    }

    theta = asin < 0 ? -theta : theta;
    angle.rotateRad(theta);
  }
}
