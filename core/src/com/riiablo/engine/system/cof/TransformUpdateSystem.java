package com.riiablo.engine.system.cof;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.cof.TransformUpdate;

public class TransformUpdateSystem extends IteratingSystem {
  private final ComponentMapper<TransformUpdate> updateComponent = ComponentMapper.getFor(TransformUpdate.class);
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<AnimationComponent> animationComponent = ComponentMapper.getFor(AnimationComponent.class);

  public TransformUpdateSystem() {
    super(Family.all(TransformUpdate.class, CofComponent.class, AnimationComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    TransformUpdate updateComponent = this.updateComponent.get(entity);
    int flags = updateComponent.flags;
    assert flags != Dirty.NONE;

    CofComponent cofComponent = this.cofComponent.get(entity);
    COF cof = cofComponent.cof;
    byte[] transform = cofComponent.transform;

    AnimationComponent animationComponent = this.animationComponent.get(entity);
    Animation animation = animationComponent.animation;

    for (int l = 0, numLayers = cof.getNumLayers(); l < numLayers; l++) {
      int component = cofComponent.cof.getLayer(l).component;
      if (!Dirty.isDirty(flags, component)) continue;
      // FIXME: NPE check here shouldn't be needed -- see note in Engine#createPlayer
      Animation.Layer layer = animation.getLayer(component);
      if (layer != null) layer.setTransform(transform[component]);
    }

    entity.remove(TransformUpdate.class);
  }
}
