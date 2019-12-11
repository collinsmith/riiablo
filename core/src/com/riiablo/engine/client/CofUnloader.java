package com.riiablo.engine.client;

import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.engine.client.component.CofDescriptor;
import com.riiablo.engine.server.event.CofChangeEvent;
import com.riiablo.codec.COF;

import net.mostlyoriginal.api.event.common.Subscribe;

@All(CofDescriptor.class)
public class CofUnloader extends BaseEntitySystem {
  private static final String TAG = "CofUnloader";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_EVENTS = DEBUG && true;

  protected ComponentMapper<CofDescriptor> mCofDescriptor;

  @Override
  protected void processSystem() {}

  @Subscribe
  public void onCofChanged(CofChangeEvent event) {
    if (DEBUG_EVENTS) Gdx.app.debug(TAG, "onCofChanged");
    unload(event.entityId);
  }

  @Override
  protected void dispose() {
    IntBag entities = getEntityIds();
    for (int i = 0, size = entities.size(); i < size; i++) {
      int id = entities.get(i);
      unload(id);
    }
  }

  private void unload(int entityId) {
    if (!mCofDescriptor.has(entityId)) return;
    CofDescriptor cofDescriptor = mCofDescriptor.get(entityId);
    AssetDescriptor<COF> descriptor = cofDescriptor.descriptor;
    if (DEBUG) Gdx.app.debug(TAG, "Unloading " + descriptor.fileName);
    Riiablo.assets.unload(descriptor.fileName);
    mCofDescriptor.remove(entityId);
  }
}
