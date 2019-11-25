package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ai.utils.Collision;
import com.badlogic.gdx.ai.utils.Ray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Pools;
import com.riiablo.Riiablo;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.engine.Flags;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.Box2DComponent;
import com.riiablo.engine.component.HoveredComponent;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.PathfindComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.SizeComponent;
import com.riiablo.engine.component.TargetComponent;
import com.riiablo.engine.component.VelocityComponent;
import com.riiablo.item.Item;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;
import com.riiablo.map.RenderSystem;
import com.riiablo.map.pfa.GraphPath;

import java.util.Iterator;

public class TouchMovementSystem extends EntitySystem {
  private final ComponentMapper<TargetComponent> targetComponent = ComponentMapper.getFor(TargetComponent.class);
  private final ComponentMapper<PathfindComponent> pathfindComponent = ComponentMapper.getFor(PathfindComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<SizeComponent> sizeComponent = ComponentMapper.getFor(SizeComponent.class);
  private final ComponentMapper<InteractableComponent> interactableComponent = ComponentMapper.getFor(InteractableComponent.class);
  private final ComponentMapper<VelocityComponent> velocityComponent = ComponentMapper.getFor(VelocityComponent.class);
  private final ComponentMapper<Box2DComponent> box2DComponent = ComponentMapper.getFor(Box2DComponent.class);

  private final Family hoveredFamily = Family.all(HoveredComponent.class).get();
  private ImmutableArray<Entity> hoveredEntities;

  private final Vector2 tmpVec2 = new Vector2();
  private final Collision<Vector2> collision = new Collision<>(new Vector2(), new Vector2());
  private final Ray<Vector2> ray = new Ray<>(new Vector2(), new Vector2());

  IsometricCamera iso;
  RenderSystem renderer;
  boolean requireRelease;

  public TouchMovementSystem(IsometricCamera iso, RenderSystem renderer) {
    super();
    this.iso = iso;
    this.renderer = renderer;
  }

  @Override
  public void addedToEngine(Engine engine) {
    super.addedToEngine(engine);
    hoveredEntities = engine.getEntitiesFor(hoveredFamily);
  }

  @Override
  public void removedFromEngine(Engine engine) {
    super.removedFromEngine(engine);
    hoveredEntities = null;
  }

  @Override
  public void update(float delta) {
    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && UIUtils.shift()) {
      // static primary cast
    } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
      // secondary cast
    } else {
      updateLeft();
    }
  }

  private void updateLeft() {
    Entity src = renderer.getSrc();
    boolean pressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
    if (pressed && !requireRelease) {
      Item cursor = Riiablo.cursor.getItem();
      if (cursor != null) {
        PositionComponent positionComponent = this.positionComponent.get(src);
        iso.toTile(tmpVec2.set(positionComponent.position));

        Riiablo.cursor.setItem(null);
        Entity item = ((com.riiablo.engine.Engine) getEngine()).createItem(cursor, tmpVec2);
        Riiablo.engine2.addEntity(item);
        requireRelease = true;
        return;
      }

      // exiting dialog should block all input until button is released to prevent menu from closing the following frame
      if (Riiablo.game.getDialog() != null) {
        Riiablo.game.setDialog(null);
        requireRelease = true;
        return;
      } else if (Riiablo.game.getMenu() != null) {
        Riiablo.game.setMenu(null, null);
      }

      // set target entity -- unsets and interacts when within range
      boolean touched = touchDown(src);
      if (!touched) {
        iso.agg(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY())).unproject().toWorld();
        setTarget(src, tmpVec2);
      }
    } else if (!pressed) {
      if (!pathfindComponent.has(src)) {
        src.remove(PathfindComponent.class);
        velocityComponent.get(src).velocity.setZero();
      }
      requireRelease = false;
      TargetComponent targetComponent = this.targetComponent.get(src);
      if (targetComponent != null) {
        Entity target = targetComponent.target;
        Vector2 srcPos = this.positionComponent.get(src).position;
        Vector2 targetPos = this.positionComponent.get(target).position;
        // not interactable -> attacking? check weapon range to auto attack or cast spell
        InteractableComponent interactableComponent = this.interactableComponent.get(target);
        if (interactableComponent != null && srcPos.dst(targetPos) <= interactableComponent.range) {
          setTarget(src, (Entity) null);
          interactableComponent.interactor.interact(src, target);
        }
      }
    }
  }

  private boolean touchDown(Entity src) {
    if (hoveredEntities.size() == 0) return false;
    Entity target = hoveredEntities.first();
    setTarget(src, target);
    return true;
  }

  private void setTarget(Entity src, Entity target) {
    if (target == null) {
      src.remove(TargetComponent.class);
      setTarget(src, (Vector2) null);
    } else {
      TargetComponent targetComponent = com.riiablo.engine.Engine.getOrCreateComponent(src, getEngine(), TargetComponent.class, this.targetComponent);
      targetComponent.target = target;
      PositionComponent positionComponent = this.positionComponent.get(target);
      setTarget(src, positionComponent.position);
    }
  }

  private void setTarget(Entity src, Vector2 target) {
    if (target == null) {
      src.remove(PathfindComponent.class);
      velocityComponent.get(src).velocity.setZero();
      return;
    }

    Map map = renderer.getMap();
    Vector2 position = this.positionComponent.get(src).position;
    int flags = DT1.Tile.FLAG_BLOCK_WALK;
    int size = this.sizeComponent.get(src).size;
    GraphPath path = Pools.obtain(GraphPath.class);
    boolean success = findPath(map, src, position, target, flags, size, path);
    if (!success) {
      ray.set(position, target);
      success = map.castRay(ray, flags, size, collision);
      if (success) {
        success = findPath(map, src, position, collision.point, flags, size, path);
        if (!success || path.getCount() <= 1) {
          src.remove(PathfindComponent.class);
          Pools.free(path);

          tmpVec2.set(target);

          AngleComponent angleComponent = src.getComponent(AngleComponent.class);
          angleComponent.target.set(tmpVec2.sub(position)).nor();

          VelocityComponent velocityComponent = src.getComponent(VelocityComponent.class);
          velocityComponent.velocity.set(tmpVec2)
              .setLength((src.flags & Flags.RUNNING) == Flags.RUNNING
                  ? velocityComponent.runSpeed : velocityComponent.walkSpeed);
          box2DComponent.get(src).body.setLinearVelocity(velocityComponent.velocity);
        }
      } else {
        Pools.free(path);
      }
    }
  }

  private boolean findPath(Map map, Entity src, Vector2 position, Vector2 target, int flags, int size, GraphPath path) {
    boolean success = map.findPath(position, target, flags, size, path);
    if (success) {
      map.smoothPath(flags, size, path);
      PathfindComponent pathfindComponent = com.riiablo.engine.Engine
          .getOrCreateComponent(src, getEngine(), PathfindComponent.class, this.pathfindComponent);
      pathfindComponent.path = path;
      pathfindComponent.targets = path.vectorIterator();
      pathfindComponent.targets.next(); // consume src position
      Iterator<Vector2> targets = pathfindComponent.targets;
      if (targets.hasNext()) {
        pathfindComponent.target.set(targets.next());
      } else {
        pathfindComponent.target.set(position);
      }
    }

    return success;
  }
}







































