package com.riiablo.engine.system.debug;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.riiablo.Riiablo;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.engine.component.PathfindComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.RenderSystem;
import com.riiablo.map.pfa.GraphPath;
import com.riiablo.map.pfa.Point2;

public class PathfindDebugSystem extends EntitySystem {
  private final float BOX_SIZE = 8;
  private final float HALF_BOX = BOX_SIZE / 2;

  private final ComponentMapper<PathfindComponent> pathfindComponent = ComponentMapper.getFor(PathfindComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final Family family = Family.all(PathfindComponent.class, PositionComponent.class).get();
  private ImmutableArray<Entity> entities;

  private final Vector2 tmpVec2a = new Vector2();
  private final Vector2 tmpVec2b = new Vector2();

  private final IsometricCamera iso;
  private final RenderSystem renderer;
  private final PaletteIndexedBatch batch;
  private final ShapeRenderer shapes;

  public PathfindDebugSystem(IsometricCamera iso, RenderSystem renderer, PaletteIndexedBatch batch, ShapeRenderer shapes) {
    super();
    this.iso = iso;
    this.renderer = renderer;
    this.batch = batch;
    this.shapes = shapes;
    setProcessing(false);
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    entities = engine.getEntitiesFor(family);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    super.removedFromEngine(engine);
    entities = null;
  }

  @Override
  public void update(float delta) {
    for (int i = 0, size = entities.size(); i < size; i++) {
      Entity entity = entities.get(i);
      PositionComponent positionComponent = this.positionComponent.get(entity);
      if (!renderer.withinRadius(positionComponent.position)) continue;
      PathfindComponent pathfindComponent = this.pathfindComponent.get(entity);
      shapes.setProjectionMatrix(iso.combined);
      shapes.begin(ShapeRenderer.ShapeType.Filled); {
        drawDebugShapes(pathfindComponent);
      } shapes.end();

      batch.begin(); {
        batch.setShader(null);
        drawDebugBatch(pathfindComponent);
      } batch.end();
      batch.setShader(Riiablo.shader);
    }
  }

  private void drawDebugShapes(PathfindComponent pathfindComponent) {
    GraphPath path = pathfindComponent.path;
    Point2 point;

    tmpVec2a.setZero();
    tmpVec2b.setZero();
    for (int i = 0; i < path.getCount(); i++, tmpVec2a.set(tmpVec2b)) {
      point = path.get(i);
      iso.toScreen(point.x, point.y, tmpVec2b);
      if (tmpVec2a.isZero()) continue;
      shapes.setColor(Color.PURPLE);
      shapes.rectLine(tmpVec2a, tmpVec2b, 2);
    }

    for (int i = 0; i < path.getCount(); i++) {
      point = path.get(i);
      iso.toScreen(point.x, point.y, tmpVec2a).sub(HALF_BOX, HALF_BOX);
      shapes.setColor(Color.WHITE);
      shapes.rect(tmpVec2a.x, tmpVec2a.y, BOX_SIZE, BOX_SIZE);
    }
  }

  private void drawDebugBatch(PathfindComponent pathfindComponent) {
    GraphPath path = pathfindComponent.path;
    Point2 point;

    for (int i = 0; i < path.getCount(); i++) {
      point = path.get(i);
      iso.toScreen(point.x, point.y, tmpVec2a).sub(0, BOX_SIZE);
      Riiablo.fonts.consolas12.draw(batch, Integer.toString(i), tmpVec2a.x, tmpVec2a.y, 0, Align.center, false);
    }
  }
}
