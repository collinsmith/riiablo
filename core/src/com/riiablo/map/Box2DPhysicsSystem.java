package com.riiablo.map;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IntervalIteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.riiablo.engine.component.Box2DComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.VelocityComponent;

import java.util.Arrays;

public class Box2DPhysicsSystem extends IntervalIteratingSystem implements EntityListener {
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<Box2DComponent> box2dComponent = ComponentMapper.getFor(Box2DComponent.class);

  private final float timeStep;

  private Map map;
  public World world;

  private final ObjectMap<Entity, Body> bodies = new ObjectMap<>();

  public Box2DPhysicsSystem(float timeStep) {
    super(Family.all(PositionComponent.class, VelocityComponent.class, Box2DComponent.class).get(), timeStep);
    this.timeStep = timeStep;
    Box2D.init();
    world = new World(Vector2.Zero, true);
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    engine.addEntityListener(Family.all(Box2DComponent.class).get(), this);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    super.removedFromEngine(engine);
    engine.removeEntityListener(this);
    world.dispose();
  }

  @Override
  public void entityAdded(Entity entity) {
    Box2DComponent box2DComponent = this.box2dComponent.get(entity);
    bodies.put(entity, box2DComponent.body);
  }

  @Override
  public void entityRemoved(Entity entity) {
    Body body = bodies.remove(entity);
    world.destroyBody(body);
  }

  public void setMap(Map map) {
    if (this.map != map) {
      this.map = map;
      createBodies();
    }
  }

  private void createBodies() {
    IntMap<Filter> filters = new IntMap<>();

    BodyDef def = new BodyDef();
    def.type = BodyDef.BodyType.StaticBody;
    def.awake = false;
    def.allowSleep = true;
    def.fixedRotation = true;
    boolean[][] handled = new boolean[1000][1000];
    for (Map.Zone zone : new Array.ArrayIterator<>(map.zones)) {
      for (boolean[] a : handled) Arrays.fill(a, false);
      for (int y = 0, ty = zone.y, height = zone.height; y < height; y++, ty++) {
        for (int x = 0, tx = zone.x, width = zone.width; x < width; x++, tx++) {
          if (handled[y][x]) continue;
          int flags = map.flags(tx, ty);
          if (flags != 0) {
            int endX = tx + 1;
            while (endX < width && map.flags(endX, ty) == flags) {
              endX++;
            }

            int lenX = endX - tx;
            int endY = ty + 1;

            int dy = y + 1;
            while (dy < height && allEqual(map, tx, endY, lenX, flags)) {
              Arrays.fill(handled[dy], x, x + lenX, true);
              dy++;
              endY++;
            }

            int lenY = endY - ty;
            def.position.set((endX + tx) / 2f, (endY + ty) / 2f);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(lenX / 2f, lenY / 2f);

            Filter filter = filters.get(flags);
            if (filter == null) {
              filters.put(flags, filter = new Filter());
              filter.categoryBits = 0xFF;
              filter.maskBits     = (short) flags;
              filter.groupIndex   = 0;
            }

            Body body = world.createBody(def);
            Fixture f = body.createFixture(shape, 0);
            f.setFilterData(filter);

            shape.dispose();

            x += lenX - 1;
            tx = endX - 1;
          }
        }
      }
    }
  }

  private static boolean allEqual(Map map, int x, int y, int len, int flags) {
    len += x;
    while (x < len && map.flags(x, y) == flags) x++;
    return x == len;
  }

  @Override
  protected void updateInterval() {
    world.step(timeStep, 6, 2);
    super.updateInterval();
  }

  @Override
  protected void processEntity(Entity entity) {
    Box2DComponent box2DComponent = this.box2dComponent.get(entity);
    PositionComponent positionComponent = this.positionComponent.get(entity);
    positionComponent.position.set(box2DComponent.body.getPosition());
  }
}
