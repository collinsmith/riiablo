package com.riiablo.engine.client.debug;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.riiablo.camera.IsometricCamera;
import com.riiablo.map.RenderSystem;
import com.riiablo.profiler.GpuSystem;

@GpuSystem
public class RenderSystemDebugger extends BaseSystem {
  protected RenderSystem renderer;

  @Wire(name = "iso")
  protected IsometricCamera iso;

  @Wire(name = "shapes")
  protected ShapeRenderer shapes;

  @Override
  protected void initialize() {
    setEnabled(false);
  }

  @Override
  protected void begin() {
    shapes.identity();
    shapes.setProjectionMatrix(iso.combined);
    shapes.setAutoShapeType(true);
    shapes.begin(ShapeRenderer.ShapeType.Line);
  }

  @Override
  protected void end() {
    shapes.end();
  }

  @Override
  protected void processSystem() {
    renderer.drawDebug(shapes);
  }
}
