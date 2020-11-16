package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Exclude;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.engine.Engine;
import com.riiablo.engine.client.component.CofDescriptor;
import com.riiablo.engine.client.component.CofWrapper;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.event.CofChangeEvent;
import com.riiablo.codec.COF;

import net.mostlyoriginal.api.event.common.Subscribe;
import net.mostlyoriginal.api.system.core.PassiveSystem;

@All({CofReference.class, Class.class})
@Exclude(CofWrapper.class)
public class CofResolver extends PassiveSystem {
  private static final String TAG = "CofResolver";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_EVENTS = DEBUG && true;

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<CofWrapper> mCofWrapper;
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<CofDescriptor> mCofDescriptor;

  @Subscribe
  public void onCofChanged(CofChangeEvent event) {
    if (DEBUG_EVENTS) Gdx.app.debug(TAG, "onCofChanged");
    mCofWrapper.remove(event.entityId);
    updateCof(event.entityId);
  }

  private void updateCof(int entityId) {
    Class.Type type = mClass.get(entityId).type;
    CofReference reference = mCofReference.get(entityId);
    String name = reference.token + type.getMode(reference.mode) + Engine.getWClass(reference.wclass);
    COF cof = null;//type.getCOFs().lookup(name);
    if (cof == null) {
      mCofWrapper.remove(entityId);
      CofDescriptor cofDescriptor = mCofDescriptor.create(entityId);
      cofDescriptor.descriptor = new AssetDescriptor<>(formatCofPath(type, reference, name), COF.class);
      if (DEBUG) Gdx.app.debug(TAG, name + "=" + cofDescriptor.descriptor.fileName);
      return;
    }

    if (DEBUG) Gdx.app.debug(TAG, name + "=" + cof);
    mCofWrapper.create(entityId).cof = cof;
  }

  private static String formatCofPath(Class.Type type, CofReference reference, String name) {
    return type.PATH + '\\' + reference.token + "\\cof\\" + name + ".cof";
  }
}
