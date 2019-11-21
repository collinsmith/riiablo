package com.riiablo.map;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IntervalIteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.engine.component.Box2DComponent;
import com.riiablo.engine.component.ObjectComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.SizeComponent;
import com.riiablo.engine.component.TypeComponent;
import com.riiablo.engine.component.VelocityComponent;

import java.util.Arrays;

public class Box2DPhysicsSystem extends IntervalIteratingSystem implements EntityListener, Disposable {
  private static final String TAG = "Box2DPhysicsSystem";
  private static final boolean DEBUG = true;

  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<Box2DComponent> box2dComponent = ComponentMapper.getFor(Box2DComponent.class);
  private final ComponentMapper<SizeComponent> sizeComponent = ComponentMapper.getFor(SizeComponent.class);
  private final ComponentMapper<TypeComponent> typeComponent = ComponentMapper.getFor(TypeComponent.class);

  private final ComponentMapper<ObjectComponent> objectComponent = ComponentMapper.getFor(ObjectComponent.class);
  private final Family objectFamily = Family.all(ObjectComponent.class, SizeComponent.class, PositionComponent.class).get();
  private ImmutableArray<Entity> objectEntities;

  private final float timeStep;

  private final BodyDef wallBodyDef = new BodyDef() {{
    type = BodyDef.BodyType.StaticBody;
    awake = false;
    allowSleep = true;
    fixedRotation = true;
  }};
  private final BodyDef objectBodyDef = new BodyDef() {{
    type = BodyDef.BodyType.StaticBody;
    fixedRotation = true;
  }};
  private final BodyDef monsterBodyDef = new BodyDef() {{
    type = BodyDef.BodyType.DynamicBody;
    fixedRotation = true;
  }};
  private final BodyDef playerBodyDef = monsterBodyDef;

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
  public void dispose() {
    world.dispose();
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    engine.addEntityListener(Family.all(TypeComponent.class, PositionComponent.class, SizeComponent.class, Box2DComponent.class).get(), this);
    objectEntities = engine.getEntitiesFor(objectFamily);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    super.removedFromEngine(engine);
    engine.removeEntityListener(this);
    objectEntities = null;
    dispose();
  }

  @Override
  public void entityAdded(Entity entity) {
    Box2DComponent box2DComponent = com.riiablo.engine.Engine
        .getOrCreateComponent(entity, getEngine(), Box2DComponent.class, box2dComponent);
    if (box2DComponent.body == null) box2DComponent.body = createBody(entity);
    if (box2DComponent.body == null) {
      entity.remove(Box2DComponent.class);
      return;
    }
    bodies.put(entity, box2DComponent.body);
  }

  @Override
  public void entityRemoved(Entity entity) {
    Body body = bodies.remove(entity);
    if (body == null) return;
    world.destroyBody(body);
  }

  public void setMap(Map map, IsometricCamera iso) {
    if (this.map != map) {
      this.map = map;
      Vector2 tileOffset = iso.getTileOffset(new Vector2()).scl(-1); // offset inverse of tile offset
      createBodies(tileOffset);
      if (DEBUG) Gdx.app.debug(TAG, "bodies=" + world.getBodyCount());
    }
  }

  private Body createBody(Entity entity) {
    SizeComponent sizeComponent = this.sizeComponent.get(entity);
    TypeComponent typeComponent = this.typeComponent.get(entity);
    if (sizeComponent.size == SizeComponent.INSIGNIFICANT && typeComponent.type != TypeComponent.Type.OBJ) return null;

    PositionComponent positionComponent = this.positionComponent.get(entity);

    Body body;
    switch (typeComponent.type) {
      case OBJ: {
          ObjectComponent objectComponent = entity.getComponent(ObjectComponent.class);
          objectBodyDef.position.set(positionComponent.position);
          body = world.createBody(objectBodyDef);
          PolygonShape shape = new PolygonShape(); {
            shape.setAsBox(objectComponent.base.SizeX / 2f, objectComponent.base.SizeY / 2f);
            body.createFixture(shape, 1f);
          } shape.dispose();
          if (map != null) {
            map.or(positionComponent.position, objectComponent.base.SizeX, objectComponent.base.SizeY, DT1.Tile.FLAG_BLOCK_WALK);
          }
        }
        break;
      case MON: {
          monsterBodyDef.position.set(positionComponent.position);
          body = world.createBody(monsterBodyDef);
          CircleShape shape = new CircleShape(); {
            shape.setRadius(sizeComponent.size / 2f);
            Fixture fixture = body.createFixture(shape, 1f);
            //fixture.setSensor(true);
          } shape.dispose();
        }
        break;
      case PLR: {
          playerBodyDef.position.set(positionComponent.position);
          body = world.createBody(playerBodyDef);
          CircleShape shape = new CircleShape(); {
            shape.setRadius(sizeComponent.size / 2f);
            Fixture fixture = body.createFixture(shape, 1f);
            //fixture.setSensor(true);
          } shape.dispose();
        }
        break;
      default:
        body = null;
    }
    return body;
  }

  private void createBodies(Vector2 offset) {
    IntMap<Filter> filters = new IntMap<>();

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
            wallBodyDef.position.set((endX + tx) / 2f, (endY + ty) / 2f).add(offset);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(lenX / 2f, lenY / 2f);

            Filter filter = filters.get(flags);
            if (filter == null) {
              filters.put(flags, filter = new Filter());
              filter.categoryBits = 0xFF;
              filter.maskBits     = (short) flags;
              filter.groupIndex   = 0;
            }

            Body body = world.createBody(wallBodyDef);
            Fixture f = body.createFixture(shape, 0);
            f.setFilterData(filter);

            shape.dispose();

            x += lenX - 1;
            tx = endX - 1;
          }
        }
      }
    }

    for (Entity entity : objectEntities) {
      PositionComponent positionComponent = this.positionComponent.get(entity);
      ObjectComponent objectComponent = this.objectComponent.get(entity);
      map.or(positionComponent.position, objectComponent.base.SizeX, objectComponent.base.SizeY, DT1.Tile.FLAG_BLOCK_WALK);
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
