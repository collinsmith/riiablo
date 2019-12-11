package com.riiablo.engine.client.debug;

import com.artemis.BaseSystem;
import com.artemis.annotations.Wire;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.map.Box2DPhysics;
import com.riiablo.map.RenderSystem;

public class Box2DDebugger extends BaseSystem {
  private static final float BOX2D_ZOOM_FACTOR = 0.04419419f;

  protected RenderSystem renderSystem;
  protected Box2DPhysics physicsSystem;

  @Wire(name = "iso")
  protected IsometricCamera iso;

  private OrthographicCamera camera;
  private Box2DDebugRenderer renderer;

  @Override
  protected void initialize() {
    setEnabled(false);
    renderer = new Box2DDebugRenderer();
    camera = new OrthographicCamera();
    camera.setToOrtho(true);
    camera.near = -1024;
    camera.far  =  1024;
    camera.rotate(Vector3.X, -60);
    camera.rotate(Vector3.Z, -45);
  }

  @Override
  protected void processSystem() {
    camera.position.set(iso.position, 0);
    camera.zoom = BOX2D_ZOOM_FACTOR * renderSystem.zoom();
    camera.update();
    renderer.render(physicsSystem.getPhysics(), camera.combined);
  }
}
