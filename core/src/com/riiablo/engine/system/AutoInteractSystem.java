package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.riiablo.engine.component.HoveredComponent;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.SelectableComponent;
import com.riiablo.map.RenderSystem;

import java.util.Comparator;

public class AutoInteractSystem extends IteratingSystem implements Comparator<Entity> {
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);
  private final ComponentMapper<InteractableComponent> interactableComponent = ComponentMapper.getFor(InteractableComponent.class);
  private final ComponentMapper<HoveredComponent> hoveredComponent = ComponentMapper.getFor(HoveredComponent.class);

  private final Vector2 position = new Vector2();
  private final Array<Entity> sortedEntities = new Array<>(false, 16);
  private final ImmutableArray<Entity> entities = new ImmutableArray<>(sortedEntities);

  private RenderSystem renderer;
  private float scalar;

  public AutoInteractSystem(RenderSystem renderer, float scalar) {
    super(Family.all(PositionComponent.class, SelectableComponent.class, InteractableComponent.class).get());
    this.renderer = renderer;
    this.scalar = scalar;
  }

  public ImmutableArray<Entity> getEntities() {
    return entities;
  }

  @Override
  public void update(float delta) {
    Entity src = renderer.getSrc();
    PositionComponent positionComponent = this.positionComponent.get(src);
    position.set(positionComponent.position);
    sortedEntities.clear();
    super.update(delta);
    sortedEntities.sort(this);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    PositionComponent positionComponent = this.positionComponent.get(entity);
    InteractableComponent interactableComponent = this.interactableComponent.get(entity);
    setHovered(entity, position.dst(positionComponent.position) <= interactableComponent.range * scalar);
  }

  private void setHovered(Entity entity, boolean b) {
    if (b) {
      if (!hoveredComponent.has(entity)) entity.add(getEngine().createComponent(HoveredComponent.class));
      sortedEntities.add(entity);
    } else {
      entity.remove(HoveredComponent.class);
    }
  }

  @Override
  public int compare(Entity e1, Entity e2) {
    Vector2 p1 = positionComponent.get(e1).position;
    Vector2 p2 = positionComponent.get(e2).position;
    float d1 = position.dst2(p1);
    float d2 = position.dst2(p2);
    return Float.compare(d1, d2);
  }
}
