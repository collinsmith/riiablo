package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.SystemPriority;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.BBoxComponent;
import com.riiablo.engine.component.CofComponent;

@DependsOn(CofLoaderSystem.class)
public class AnimationLoaderSystem extends IteratingSystem {
  private static final String TAG = "AnimationLoaderSystem";

  private static final boolean DEBUG      = true;
  private static final boolean DEBUG_LOAD = DEBUG && !true;

  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<AnimationComponent> animComponent = ComponentMapper.getFor(AnimationComponent.class);
  private final ComponentMapper<BBoxComponent> boxComponent = ComponentMapper.getFor(BBoxComponent.class);

  public AnimationLoaderSystem() {
    super(Family.all(CofComponent.class, AnimationComponent.class).get(), SystemPriority.AnimationLoaderSystem);
  }

  @Override
  public void update(float deltaTime) {
    //Riiablo.assets.update();
    super.update(deltaTime);
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    AnimationComponent animComponent = this.animComponent.get(entity);
    CofComponent cofComponent = this.cofComponent.get(entity);

    Animation anim = animComponent.animation;

    boolean changed = false;
    COF cof = cofComponent.cof;
    if (cof == null) return;
    anim.setCOF(cof);
    for (int l = 0, numLayers = cof.getNumLayers(); l < numLayers; l++) {
      COF.Layer layer = cof.getLayer(l);
      if (!Dirty.isDirty(cofComponent.load, layer.component)) continue;
      if (cofComponent.component[layer.component] == CofComponent.COMPONENT_NIL) {
        anim.setLayer(layer, null, false);
        changed = true;
        continue;
      }

      AssetDescriptor<? extends DC> descriptor = cofComponent.layer[layer.component];
      if (Riiablo.assets.isLoaded(descriptor)) {
        cofComponent.load &= ~(1 << layer.component);
        Gdx.app.debug(TAG, "finished loading " + descriptor);
        DC dc = Riiablo.assets.get(descriptor);
        anim.setLayer(layer, dc, false);
        changed = true;
      }
    }

    if (changed) anim.updateBox();
    if (DEBUG_LOAD) Gdx.app.debug(TAG, "load layers: " + Dirty.toString(cofComponent.load));
  }
}
