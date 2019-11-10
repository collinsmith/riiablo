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
import com.riiablo.engine.component.CofComponent;

@DependsOn(CofLoaderSystem.class)
public class AnimationLoaderSystem extends IteratingSystem {
  private static final String TAG = "AnimationLoaderSystem";

  private static final boolean DEBUG      = true;
  private static final boolean DEBUG_LOAD = DEBUG && !true;

  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);
  private final ComponentMapper<AnimationComponent> animComponent = ComponentMapper.getFor(AnimationComponent.class);

  public AnimationLoaderSystem() {
    super(Family.all(CofComponent.class, AnimationComponent.class).get(), SystemPriority.AnimationLoaderSystem);
  }

  @Override
  public void update(float delta) {
    //Riiablo.assets.update();
    super.update(delta);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    AnimationComponent animComponent = this.animComponent.get(entity);
    CofComponent cofComponent = this.cofComponent.get(entity);

    Animation anim = animComponent.animation;

    boolean changed = false;
    COF cof = cofComponent.cof;
    if (cof == null) return;
    // FIXME: logic here needs to be looked into -- should below operations be performed when cof didn't change?
    boolean newCof = anim.setCOF(cof);
    if (newCof && cofComponent.speed != CofComponent.SPEED_NULL) {
      anim.setFrameDelta(cofComponent.speed);
    }
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
        int flag = (1 << layer.component);
        cofComponent.load &= ~flag;
        cofComponent.update &= ~flag;
        Gdx.app.debug(TAG, "finished loading " + descriptor);
        DC dc = Riiablo.assets.get(descriptor);
        anim.setLayer(layer, dc, false)
            .setTransform(cofComponent.transform[layer.component])
            .setAlpha(cofComponent.alpha[layer.component])
            ;
        changed = true;
      }
    }

    if (changed) anim.updateBox();
    if (DEBUG_LOAD) Gdx.app.debug(TAG, "load layers: " + Dirty.toString(cofComponent.load));
  }
}
