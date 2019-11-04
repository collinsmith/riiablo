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
import com.riiablo.engine.component.PathComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DS1;
import com.riiablo.map.RenderSystem;

public class PathDebugSystem extends EntitySystem {
  private final float BOX_SIZE = 8;
  private final float HALF_BOX = BOX_SIZE / 2;

  private final ComponentMapper<PathComponent> pathComponent = ComponentMapper.getFor(PathComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final Family family = Family.all(PathComponent.class, PositionComponent.class).get();
  private ImmutableArray<Entity> entities;

  private final Vector2 tmpVec2a = new Vector2();
  private final Vector2 tmpVec2b = new Vector2();

  private final IsometricCamera iso;
  private final RenderSystem renderer;
  private final PaletteIndexedBatch batch;
  private final ShapeRenderer shapes;

  public PathDebugSystem(IsometricCamera iso, RenderSystem renderer, PaletteIndexedBatch batch, ShapeRenderer shapes) {
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
      PathComponent pathComponent = this.pathComponent.get(entity);
      shapes.setProjectionMatrix(iso.combined);
      shapes.begin(ShapeRenderer.ShapeType.Filled); {
        drawDebugShapes(pathComponent);
      } shapes.end();

      batch.begin(); {
        batch.setShader(null);
        drawDebugBatch(pathComponent);
      } batch.end();
      batch.setShader(Riiablo.shader);
    }
  }

  private void drawDebugShapes(PathComponent pathComponent) {
    DS1.Path path = pathComponent.path;
    DS1.Path.Point point;

    tmpVec2a.setZero();
    tmpVec2b.setZero();
    for (int i = 0; i < path.numPoints; i++, tmpVec2a.set(tmpVec2b)) {
      point = path.points[i];
      iso.toScreen(point.x, point.y, tmpVec2b);
      if (tmpVec2a.isZero()) continue;
      shapes.setColor(Color.PURPLE);
      shapes.rectLine(tmpVec2a, tmpVec2b, 2);
    }

    if (path.numPoints > 1) {
      point = path.points[0];
      iso.toScreen(point.x, point.y, tmpVec2a);
      shapes.setColor(Color.PURPLE);
      shapes.rectLine(tmpVec2b, tmpVec2a, 2);
    }

    for (int i = 0; i < path.numPoints; i++) {
      point = path.points[i];
      iso.toScreen(point.x, point.y, tmpVec2a).sub(HALF_BOX, HALF_BOX);
      shapes.setColor(Color.WHITE);
      shapes.rect(tmpVec2a.x, tmpVec2a.y, BOX_SIZE, BOX_SIZE);
    }
  }

  private void drawDebugBatch(PathComponent pathComponent) {
    DS1.Path path = pathComponent.path;
    DS1.Path.Point point;

    for (int i = 0; i < path.numPoints; i++) {
      point = path.points[i];
      iso.toScreen(point.x, point.y, tmpVec2a).sub(0, BOX_SIZE);
      Riiablo.fonts.consolas12.draw(batch, Integer.toString(point.action), tmpVec2a.x, tmpVec2a.y, 0, Align.center, false);
    }
  }
}
