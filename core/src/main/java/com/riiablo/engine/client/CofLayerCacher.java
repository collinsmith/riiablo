package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.Engine;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.client.component.CofComponentDescriptors;
import com.riiablo.engine.client.component.CofLoadingComponents;
import com.riiablo.engine.client.component.CofWrapper;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.component.AnimData;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.event.CofChangeEvent;

import net.mostlyoriginal.api.event.common.Subscribe;

@All({
    CofLoadingComponents.class, CofWrapper.class, CofComponents.class, AnimData.class
})
public class CofLayerCacher extends IteratingSystem {
  private static final String TAG = "CofLayerCacher";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_EVENTS = DEBUG && true;

  protected ComponentMapper<AnimData> mAnimData;
  protected ComponentMapper<CofWrapper> mCofWrapper;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;
  protected ComponentMapper<CofLoadingComponents> mCofLoadingComponents;
  protected ComponentMapper<CofComponentDescriptors> mCofComponentDescriptors;

  protected CofManager cofs;

  @Override
  protected void process(int entityId) {
    CofLoadingComponents loading = mCofLoadingComponents.get(entityId);
    loading.flags = cacheDcs(entityId, loading.flags);
    if (loading.flags == Dirty.NONE) mCofLoadingComponents.remove(entityId);
  }

  @Subscribe
  public void onCofChanged(CofChangeEvent event) {
    if (DEBUG_EVENTS) Gdx.app.debug(TAG, "onCofChanged");
  }

  private int cacheDcs(int entityId, int flags) {
    COF cof = mCofWrapper.get(entityId).cof;
    int[] component = mCofComponents.get(entityId).component;
    AssetDescriptor<? extends DC>[] descriptors = mCofComponentDescriptors.get(entityId).descriptors;
    Animation animation = mAnimationWrapper.get(entityId).animation;

//    if (cof == null) return;
    // FIXME: logic here needs to be looked into -- should below operations be performed when cof didn't change?
    boolean newCof = animation.setCOF(cof);
    if (newCof) {
      AnimData animData = mAnimData.get(entityId);
      if (animData.override >= 0) {
        animation.setFrameDelta(animData.override);
      } else {
        animation.setFrameDelta(animData.speed);
      }
//      if (animData.factor > 0) {
//        System.out.println("setFrameDelta 0 " + animData.factor);
//        animation.setFrameDelta(animData.factor);
//      } else if (animData.speed == 0) {
//        System.out.println("setFrameDelta 1 " + cof.getAnimRate());
//        animation.setFrameDelta(cof.getAnimRate());
//      } else {
//        System.out.println("setFrameDelta 2 " + animData.factor);
//        animation.setFrameDelta(animData.speed);
//      }
//      if (mAnimData.get(entityId).speed == 0) {
//        Vector2 velocity = world.getEntity(entityId).getComponent(Velocity.class).velocity;
//        animation.setFrameDelta((int)(16 * velocity.len()));
//      }
    }
//    if (newCof && cofComponent.speed != CofComponent.SPEED_NULL) {
//      anim.setFrameDelta(cofComponent.speed);
//    }

    int alteredLayers = Dirty.NONE;
    for (int l = 0, size = cof.getNumLayers(); l < size; l++) {
      COF.Layer layer = cof.getLayer(l);
      int c = layer.component;
      if (!Dirty.isDirty(flags, c)) continue;
      int flag = (1 << c);
      if (component[c] == CofComponents.COMPONENT_NIL) {
        flags &= ~flag;
        alteredLayers |= flag;
        animation.setLayer(layer, null, false);
        continue;
      }

      AssetDescriptor<? extends DC> descriptor = descriptors[c];
      if (Riiablo.assets.isLoaded(descriptor)) {
        flags &= ~flag;
        alteredLayers |= flag;
        if (DEBUG) Gdx.app.debug(TAG, "Loaded[" + Engine.getComposite(c) + "] " + descriptor.fileName);
        DC dc = Riiablo.assets.get(descriptor);
        animation.setLayer(layer, dc, false);
      }
    }

    if (alteredLayers != Dirty.NONE) {
      animation.updateBox();
      cofs.updateAlpha(entityId, alteredLayers);
      cofs.updateTransform(entityId, alteredLayers);
    }
    if (DEBUG) Gdx.app.debug(TAG, "Remaining layers: " + Dirty.toString(flags));
    return flags;
  }
}
