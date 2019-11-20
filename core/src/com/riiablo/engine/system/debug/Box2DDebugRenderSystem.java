package com.riiablo.engine.system.debug;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.riiablo.map.Box2DPhysicsSystem;
import com.riiablo.map.RenderSystem;

public class Box2DDebugRenderSystem extends EntitySystem {
  private static final float BOX2D_ZOOM_FACTOR = 0.04419419f;

  RenderSystem renderSystem;
  Box2DPhysicsSystem physicsSystem;
  OrthographicCamera camera;
  Box2DDebugRenderer renderer;

  public Box2DDebugRenderSystem(RenderSystem renderSystem) {
    setProcessing(false);
    this.renderSystem = renderSystem;
    renderer = new Box2DDebugRenderer();
    camera = new OrthographicCamera();
    camera.setToOrtho(true);
    camera.near = -1024;
    camera.far  =  1024;
    camera.rotate(Vector3.X, -60);
    camera.rotate(Vector3.Z, -45);
  }

  @Override
  public void addedToEngine(Engine engine) {
    assert physicsSystem == null;
    physicsSystem = engine.getSystem(Box2DPhysicsSystem.class);
    assert physicsSystem != null;
  }

  @Override
  public void update(float delta) {
    camera.position.set(renderSystem.iso().position, 0);
    camera.zoom = BOX2D_ZOOM_FACTOR * renderSystem.zoom();
    camera.update();
    renderer.render(physicsSystem.world, camera.combined);
  }
}
