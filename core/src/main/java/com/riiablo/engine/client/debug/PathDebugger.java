package com.riiablo.engine.client.debug;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Wire;
import com.artemis.systems.IteratingSystem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;

import com.riiablo.Riiablo;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.engine.server.component.PathWrapper;
import com.riiablo.engine.server.component.Position;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DS1;
import com.riiablo.map.RenderSystem;
import com.riiablo.profiler.GpuSystem;

@GpuSystem
@All({PathWrapper.class, Position.class})
public class PathDebugger extends IteratingSystem {
  private final float BOX_SIZE = 8;
  private final float HALF_BOX = BOX_SIZE / 2;

  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<PathWrapper> mPathWrapper;

  protected RenderSystem renderer;

  @Wire(name = "iso")
  protected IsometricCamera iso;

  @Wire(name = "batch")
  protected PaletteIndexedBatch batch;

  @Wire(name = "shapes")
  protected ShapeRenderer shapes;

  private final Vector2 tmpVec2a = new Vector2();
  private final Vector2 tmpVec2b = new Vector2();

  @Override
  protected void initialize() {
    setEnabled(false);
  }

  @Override
  protected void process(int entityId) {
    Vector2 position = mPosition.get(entityId).position;
    if (!renderer.withinRadius(position)) return;
    PathWrapper pathWrapper = mPathWrapper.get(entityId);
    shapes.setProjectionMatrix(iso.combined);
    shapes.begin(ShapeRenderer.ShapeType.Filled); {
      drawDebugShapes(pathWrapper);
    } shapes.end();

    batch.begin(); {
      batch.setShader(null);
      drawDebugBatch(pathWrapper);
    } batch.end();
    batch.setShader(Riiablo.shader);
  }

  private void drawDebugShapes(PathWrapper pathWrapper) {
    DS1.Path path = pathWrapper.path;
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

  private void drawDebugBatch(PathWrapper pathWrapper) {
    DS1.Path path = pathWrapper.path;
    DS1.Path.Point point;

    for (int i = 0; i < path.numPoints; i++) {
      point = path.points[i];
      iso.toScreen(point.x, point.y, tmpVec2a).sub(0, BOX_SIZE);
      Riiablo.fonts.consolas12.draw(batch, Integer.toString(point.action), tmpVec2a.x, tmpVec2a.y, 0, Align.center, false);
    }
  }
}
