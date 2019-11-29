package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.codec.Animation;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.SequenceComponent;

public class SequenceSystem extends IteratingSystem {
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<SequenceComponent> sequenceComponent = ComponentMapper.getFor(SequenceComponent.class);
  private final ComponentMapper<AnimationComponent> animationComponent = ComponentMapper.getFor(AnimationComponent.class);

  public SequenceSystem() {
    super(Family.all(CofComponent.class, SequenceComponent.class, AnimationComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    SequenceComponent sequenceComponent = this.sequenceComponent.get(entity);
    CofComponent cofComponent = this.cofComponent.get(entity);
    Animation animation = animationComponent.get(entity).animation;
    if (cofComponent.mode != sequenceComponent.mode1) {
      cofComponent.mode = sequenceComponent.mode1;
    } else if (animation.isFinished()) {
      cofComponent.mode = sequenceComponent.mode2;
      entity.remove(SequenceComponent.class);
    }
  }
}
