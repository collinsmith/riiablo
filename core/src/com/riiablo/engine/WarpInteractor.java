package com.riiablo.engine;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import com.riiablo.Riiablo;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.Box2DComponent;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.PathfindComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.WarpComponent;
import com.riiablo.entity.Warp;
import com.riiablo.map.Map;
import com.riiablo.map.pfa.GraphPath;

import java.util.Iterator;

public class WarpInteractor implements InteractableComponent.Interactor {
  private static final String TAG = "WarpInteractor";

  private final ComponentMapper<WarpComponent> warpComponent = ComponentMapper.getFor(WarpComponent.class);
  private final ComponentMapper<MapComponent> mapComponent = ComponentMapper.getFor(MapComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<PathfindComponent> pathfindComponent = ComponentMapper.getFor(PathfindComponent.class);
  private final ComponentMapper<Box2DComponent> box2DComponent = ComponentMapper.getFor(Box2DComponent.class);

  private final Vector2 tmpVec2 = new Vector2();

  @Override
  public void interact(Entity src, Entity entity) {
    Gdx.app.log(TAG, "zim zim zala bim");
    WarpComponent warpComponent = this.warpComponent.get(entity);
    MapComponent mapComponent = this.mapComponent.get(entity);
    Map.Zone dst = mapComponent.map.findZone(warpComponent.dstLevel);
    int dstIndex = mapComponent.zone.getWarp(warpComponent.index);
    Warp dstWarp = dst.findWarp(dstIndex);
    if (dstWarp == null) throw new AssertionError("Invalid dstWarp: " + dstIndex);
    PositionComponent positionComponent = this.positionComponent.get(src);
    positionComponent.position.set(dstWarp.position());

    Box2DComponent box2DComponent = this.box2DComponent.get(src);
    if (box2DComponent != null) box2DComponent.body.setTransform(positionComponent.position, 0);

    tmpVec2.set(dstWarp.warp.ExitWalkX, dstWarp.warp.ExitWalkY);
    AngleComponent angleComponent = src.getComponent(AngleComponent.class);
    angleComponent.angle.set(tmpVec2).nor();
    angleComponent.target.set(angleComponent.angle);

    tmpVec2.set(dstWarp.position()).add(dstWarp.warp.ExitWalkX, dstWarp.warp.ExitWalkY);

    // no collision or size to make sure path is generated
    GraphPath path = Pools.obtain(GraphPath.class);
    boolean success = mapComponent.map.findPath(positionComponent.position, tmpVec2, 0, 0, path);
    if (success) {
      mapComponent.map.smoothPath(0, 0, path);
      PathfindComponent pathfindComponent = com.riiablo.engine.Engine
          .getOrCreateComponent(src, Riiablo.engine2, PathfindComponent.class, this.pathfindComponent);
      pathfindComponent.path = path;
      pathfindComponent.targets = path.vectorIterator();
      pathfindComponent.targets.next(); // consume src position
      Iterator<Vector2> targets = pathfindComponent.targets;
      if (targets.hasNext()) {
        pathfindComponent.target.set(targets.next());
      } else {
        pathfindComponent.target.set(positionComponent.position);
      }
    } else {
      Pools.free(path);
      src.remove(PathfindComponent.class);
    }
  }
}
