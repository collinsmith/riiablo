package com.riiablo.engine;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.engine.component.AIComponent;

public class AISystem extends IteratingSystem {
  private final ComponentMapper<AIComponent> aiComponent = ComponentMapper.getFor(AIComponent.class);

  public AISystem() {
    super(Family.all(AIComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    aiComponent.get(entity).ai.update(delta);
  }
}
