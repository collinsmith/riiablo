package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Pathfind;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Running;
import com.riiablo.engine.server.component.Size;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;
import com.riiablo.map.pfa.GraphPath;

import java.util.Iterator;

@All({Pathfind.class, Position.class, Velocity.class})
public class Pathfinder extends IteratingSystem {
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Size> mSize;
  protected ComponentMapper<Pathfind> mPathfind;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Running> mRunning;

  @Wire(name = "map")
  protected Map map;

  private final Vector2 tmpVec2 = new Vector2();
  private final Ray<Vector2> ray = new Ray<>(new Vector2(), new Vector2());
  private final Collision<Vector2> collision = new Collision<>(new Vector2(), new Vector2());

  @Override
  protected void process(int entityId) {
    Vector2 position0 = mPosition.get(entityId).position;
    tmpVec2.set(position0);
    Pathfind pathfind = mPathfind.get(entityId);
    Vector2 target = pathfind.target;
    Iterator<Vector2> targets = pathfind.targets;
    if (target.isZero()) return;
    if (tmpVec2.epsilonEquals(target, 0.1f)) { // TODO: tune this appropriately
      if (!targets.hasNext()) {
        findPath(entityId, null);
        return;
      }
    }

    Velocity velocity = mVelocity.get(entityId);
    float speed    = (mRunning.has(entityId) ? velocity.runSpeed : velocity.walkSpeed);
    float distance = speed * world.delta;
    float traveled = 0;
    while (traveled < distance) {
      float targetLen = tmpVec2.dst(target);
      float part = Math.min(distance - traveled, targetLen);
      if (part == 0) break;
      tmpVec2.lerp(target, part / targetLen);
      traveled += part;
      if (MathUtils.isEqual(part, targetLen, 0.1f)) {
        if (targets.hasNext()) {
          target.set(targets.next());
        } else {
          break;
        }
      }
    }

    /**
     * FIXME: there is a lot of jitter here in the direction for shorter movements because of
     *        repathing every frame-- need to create some kind of target component which is a target
     *        entity or target point and if it's down to the last remaining waypoint, set angle to
     *        the actual point or entity.
     */
    tmpVec2.sub(position0);
    mAngle.get(entityId).target.set(tmpVec2).nor();

    velocity.velocity.set(tmpVec2).setLength(speed);
  }

  public boolean findPath(int src, Vector2 target) {
    return findPath(src, target, false);
  }

  public boolean findPath(int src, Vector2 target, boolean raycast) {
    if (target == null) {
      mPathfind.remove(src);
      mVelocity.get(src).velocity.setZero();
      return false;
    }

    Vector2 position = mPosition.get(src).position;
    int flags = DT1.Tile.FLAG_BLOCK_WALK;
    int size = mSize.get(src).size;
    GraphPath path = Pools.obtain(GraphPath.class);
    boolean success = findPath(src, position, target, flags, size, path);
    if (success) return true;
    if (raycast) {
      ray.set(position, target);
      success = map.castRay(ray, flags, size, collision);
      if (success) {
        success = findPath(src, position, collision.point, flags, size, path);
        if (!success || path.getCount() <= 1) {
          tmpVec2.set(target);

          Angle angle = mAngle.get(src);
          angle.target.set(tmpVec2.sub(position)).nor();

          Velocity velocity = mVelocity.get(src);
          velocity.velocity.set(tmpVec2);

          mPathfind.remove(src);
          Pools.free(path);
          return false;
        }

        return true;
      }

      Pools.free(path);
      return false;
    } else {
      Pools.free(path);
      return false;
    }
  }

  protected boolean findPath(int src, Vector2 srcPos, Vector2 targetPos, int flags, int size, GraphPath path) {
    boolean success = map.findPath(srcPos, targetPos, flags, size, path);
    if (success) {
      map.smoothPath(flags, size, path);
      mPathfind.create(src).set(path);
    }

    return success;
  }
}
