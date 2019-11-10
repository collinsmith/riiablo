package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.SystemPriority;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.CofComponent;

public class AnimationTransformationSystem extends IteratingSystem {
  private static final String TAG = "AnimationTransformationSystem";

  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_STATE = DEBUG && !true;

  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<AnimationComponent> animComponent = ComponentMapper.getFor(AnimationComponent.class);

  public AnimationTransformationSystem() {
    super(Family.all(CofComponent.class, AnimationComponent.class).get(), SystemPriority.AnimationLoaderSystem);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    CofComponent cofComponent = this.cofComponent.get(entity);
    int flags = cofComponent.update;
    if (flags == Dirty.NONE) return;

    AnimationComponent animComponent = this.animComponent.get(entity);
    Animation animation = animComponent.animation;
    if (animation ==  null) return;

    COF cof = cofComponent.cof;
    for (int l = 0, numLayers = cof.getNumLayers(); l < numLayers; l++) {
      int component = cof.getLayer(l).component;
      if (!Dirty.isDirty(flags, component)) continue;
      Animation.Layer layer = animation.getLayer(component);
      if (layer != null) {
        layer
            .setTransform(cofComponent.transform[component])
            .setAlpha(cofComponent.alpha[component])
            ;
      }
    }

    cofComponent.update = Dirty.NONE;
  }
}
