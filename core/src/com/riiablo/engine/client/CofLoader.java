package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.annotations.Exclude;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.engine.client.component.CofDescriptor;
import com.riiablo.engine.client.component.CofWrapper;
import com.riiablo.engine.server.event.CofChangeEvent;
import com.riiablo.codec.COF;

import net.mostlyoriginal.api.event.common.Subscribe;

@All(CofDescriptor.class)
@Exclude(CofWrapper.class)
public class CofLoader extends IteratingSystem {
  private static final String TAG = "CofLoader";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_EVENTS = DEBUG && true;

  protected ComponentMapper<CofWrapper> mCofWrapper;
  protected ComponentMapper<CofDescriptor> mCofDescriptor;

  @Override
  protected void process(int entityId) {
    checkLoaded(entityId);
  }

  @Subscribe
  public void onCofChanged(CofChangeEvent event) {
    if (DEBUG_EVENTS) Gdx.app.debug(TAG, "inserted");
    AssetDescriptor<COF> descriptor = mCofDescriptor.get(event.entityId).descriptor;
    Riiablo.assets.load(descriptor);
    if (DEBUG) Gdx.app.debug(TAG, "Loading " + descriptor.fileName);
    checkLoaded(event.entityId);
  }

  @Override
  protected void inserted(int entityId) {
  }

  private void checkLoaded(int entityId) {
    AssetDescriptor<COF> descriptor = mCofDescriptor.get(entityId).descriptor;
    if (!Riiablo.assets.isLoaded(descriptor)) return;
    mCofWrapper.create(entityId).cof = Riiablo.assets.get(descriptor);
    if (DEBUG) Gdx.app.debug(TAG, "Loaded " + descriptor.fileName);
  }
}
