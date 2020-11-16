package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.badlogic.gdx.Gdx;
import com.riiablo.Riiablo;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.AnimData;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.event.CofChangeEvent;
import com.riiablo.codec.D2;

import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

@All({CofReference.class, Class.class})
public class AnimDataResolver extends PassiveSystem {
  private static final String TAG = "AnimDataResolver";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_EVENTS = DEBUG && true;

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<AnimData> mAnimData;
  protected ComponentMapper<CofReference> mCofReference;

  @Subscribe
  public void onCofChanged(CofChangeEvent event) {
    if (DEBUG_EVENTS) Gdx.app.debug(TAG, "onCofChanged");
    updateAnimData(event.entityId);
  }

  private void updateAnimData(int entityId) {
    Class.Type t = mClass.get(entityId).type;
    CofReference c = mCofReference.get(entityId);
    String cof = c.token + t.MODE[c.mode] + Engine.getWClass(c.wclass);
    D2.Entry entry = Riiablo.anim.getEntry(cof);
    if (DEBUG) Gdx.app.debug(TAG, cof + "=" + entry);
    AnimData animData = mAnimData.create(entityId);
    animData.speed     = entry.speed;
    animData.frame     = 0;
    animData.numFrames = entry.framesPerDir << 8;
    animData.keyframes = entry.data;
  }
}
