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
import com.riiablo.engine.server.component.Pathfind;
import com.riiablo.engine.server.component.Position;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.RenderSystem;
import com.riiablo.map.pfa.GraphPath;
import com.riiablo.map.pfa.Point2;
import com.riiablo.profiler.GpuSystem;

@GpuSystem
@All({Pathfind.class, Position.class})
public class PathfindDebugger extends IteratingSystem {
  private final float BOX_SIZE = 8;
  private final float HALF_BOX = BOX_SIZE / 2;

  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Pathfind> mPathfind;

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
    Position position = mPosition.get(entityId);
    if (!renderer.withinRadius(position.position)) return;
    Pathfind pathfind = mPathfind.get(entityId);
    drawDebugPath(pathfind.path, Color.PURPLE, true);
  }

  public void drawDebugPath(GraphPath path, Color lineColor, boolean drawNodes) {
    shapes.setProjectionMatrix(iso.combined);
    shapes.begin(ShapeRenderer.ShapeType.Filled); {
      drawDebugShapes(path, lineColor, drawNodes);
    } shapes.end();

    if (!drawNodes) return;
    batch.begin(); {
      batch.setShader(null);
      drawDebugBatch(path);
    } batch.end();
    batch.setShader(Riiablo.shader);
  }

  private void drawDebugShapes(GraphPath path, Color lineColor, boolean drawNodes) {
    Point2 point;

    tmpVec2a.setZero();
    tmpVec2b.setZero();
    for (int i = 0; i < path.getCount(); i++, tmpVec2a.set(tmpVec2b)) {
      point = path.get(i);
      iso.toScreen(point.x, point.y, tmpVec2b);
      if (tmpVec2a.isZero()) continue;
      shapes.setColor(lineColor);
      shapes.rectLine(tmpVec2a, tmpVec2b, 2);
    }

    if (!drawNodes) return;
    for (int i = 0; i < path.getCount(); i++) {
      point = path.get(i);
      iso.toScreen(point.x, point.y, tmpVec2a).sub(HALF_BOX, HALF_BOX);
      shapes.setColor(Color.WHITE);
      shapes.rect(tmpVec2a.x, tmpVec2a.y, BOX_SIZE, BOX_SIZE);
    }
  }

  private void drawDebugBatch(GraphPath path) {
    Point2 point;

    for (int i = 0; i < path.getCount(); i++) {
      point = path.get(i);
      iso.toScreen(point.x, point.y, tmpVec2a).sub(0, BOX_SIZE);
      Riiablo.fonts.consolas12.draw(batch, Integer.toString(i), tmpVec2a.x, tmpVec2a.y, 0, Align.center, false);
    }
  }
}
