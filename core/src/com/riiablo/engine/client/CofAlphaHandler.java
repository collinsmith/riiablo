package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.event.AlphaChangeEvent;

import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

public class CofAlphaHandler extends PassiveSystem {
  private static final String TAG = "CofAlphaHandler";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_EVENTS = DEBUG && true;

  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;

  @Subscribe
  public void onAlphaChanged(AlphaChangeEvent event) {
    if (DEBUG_EVENTS) Gdx.app.debug(TAG, "onAlphaChanged");
    updateAlpha(event.entityId, event.flags);
  }

  private void updateAlpha(int entityId, int flags) {
    float[] alpha = mCofAlphas.get(entityId).alpha;
    Animation animation = mAnimationWrapper.get(entityId).animation;
    COF cof = animation.getCOF();
    if (cof == null) return;
    for (int l = 0, size = cof.getNumLayers(); l < size; l++) {
      int c = cof.getLayer(l).component;
      if (!Dirty.isDirty(flags, c)) continue;
      Animation.Layer layer = animation.getLayer(c);
      if (layer != null) layer.setAlpha(alpha[c]);
    }
  }
}
