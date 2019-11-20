package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.engine.Flags;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.PathfindComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.VelocityComponent;
import com.riiablo.map.MapGraph;

import java.util.Iterator;

public class PathfindSystem extends IteratingSystem {
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);
  private final ComponentMapper<AngleComponent> angleComponent = ComponentMapper.getFor(AngleComponent.class);
  private final ComponentMapper<PathfindComponent> pathfindComponent = ComponentMapper.getFor(PathfindComponent.class);

  private final Vector2 position = new Vector2();

  public PathfindSystem() {
    super(Family.all(PositionComponent.class, VelocityComponent.class, PathfindComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    PositionComponent positionComponent = this.positionComponent.get(entity);
    position.set(positionComponent.position);
    VelocityComponent velocityComponent = this.velocityComponent.get(entity);
    PathfindComponent pathfindComponent = this.pathfindComponent.get(entity);
    Vector2 target = pathfindComponent.target;
    Iterator<MapGraph.Point2> targets = pathfindComponent.targets;
    if (target.isZero()) return;
    if (position.epsilonEquals(target, 0.1f)) { // TODO: tune this appropriately
      if (!targets.hasNext()) {
        velocityComponent.velocity.setZero();
        entity.remove(PathfindComponent.class);
        return;
      }
    }

    float speed    = ((entity.flags & Flags.RUNNING) == Flags.RUNNING ? velocityComponent.runSpeed : velocityComponent.walkSpeed);
    float distance = speed * delta;
    float traveled = 0;
    while (traveled < distance) {
      float targetLen = position.dst(target);
      float part = Math.min(distance - traveled, targetLen);
      if (part == 0) break;
      position.lerp(target, part / targetLen);
      traveled += part;
      if (MathUtils.isEqual(part, targetLen, 0.1f)) {
        if (targets.hasNext()) {
          MapGraph.Point2 next = targets.next();
          target.set(next.x, next.y);
        } else {
          break;
        }
      }
    }

    AngleComponent angleComponent = this.angleComponent.get(entity);
    angleComponent.target.set(position.sub(positionComponent.position)).nor();

    velocityComponent.velocity.set(position)
        .setLength((entity.flags & Flags.RUNNING) == Flags.RUNNING
            ? velocityComponent.runSpeed : velocityComponent.walkSpeed);
  }
}
