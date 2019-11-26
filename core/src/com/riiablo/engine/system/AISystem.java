package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.engine.component.AIComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.map.RenderSystem;

public class AISystem extends IteratingSystem {
  private final ComponentMapper<AIComponent> aiComponent = ComponentMapper.getFor(AIComponent.class);
  private final ComponentMapper<PositionComponent> positionComponent = ComponentMapper.getFor(PositionComponent.class);

  private final RenderSystem renderer;

  public AISystem(RenderSystem renderer) {
    super(Family.all(AIComponent.class, PositionComponent.class).get());
    this.renderer = renderer;
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    if (!renderer.withinRadius(positionComponent.get(entity).position)) return;
    aiComponent.get(entity).ai.update(delta);
  }
}
