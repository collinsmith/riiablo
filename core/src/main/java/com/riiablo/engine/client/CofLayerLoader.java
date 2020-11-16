package com.riiablo.engine.client;


import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC;
import com.riiablo.codec.DC6;
import com.riiablo.codec.DCC;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.Engine;
import com.riiablo.engine.client.component.CofComponentDescriptors;
import com.riiablo.engine.client.component.CofDirtyComponents;
import com.riiablo.engine.client.component.CofLoadingComponents;
import com.riiablo.engine.client.component.CofWrapper;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.event.CofChangeEvent;

import net.mostlyoriginal.api.event.common.Subscribe;

@All({
    CofDirtyComponents.class, CofWrapper.class, CofReference.class, Class.class,
    CofComponents.class, CofComponentDescriptors.class
})
public class CofLayerLoader extends IteratingSystem {
  private static final String TAG = "CofLayerLoader";
  private static final boolean DEBUG        = !true;
  private static final boolean DEBUG_EVENTS = DEBUG && true;

  private final StringBuilder builder = new StringBuilder(64);

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<CofWrapper> mCofWrapper;
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofDirtyComponents> mCofDirtyComponents;
  protected ComponentMapper<CofLoadingComponents> mCofLoadingComponents;
  protected ComponentMapper<CofComponentDescriptors> mCofComponentDescriptors;

  @Override
  protected void process(int entityId) {}

  @Override
  protected void inserted(int entityId) {
    if (DEBUG_EVENTS) Gdx.app.debug(TAG, "inserted");
    int flags = mCofDirtyComponents.get(entityId).flags;
    int requiresReload = loadDcs(entityId, flags);
    mCofDirtyComponents.remove(entityId);
    if (requiresReload != Dirty.NONE) mCofLoadingComponents.create(entityId).flags |= requiresReload;
  }

  @Subscribe
  public void onCofChanged(CofChangeEvent event) {
    if (DEBUG_EVENTS) Gdx.app.debug(TAG, "onCofChanged");
    mCofDirtyComponents.create(event.entityId).flags |= Dirty.ALL;
  }

  private int loadDcs(int entityId, int flags) {
    int requiresReload = Dirty.NONE;

    Class.Type type = mClass.get(entityId).type;
    CofReference reference = mCofReference.get(entityId);
    COF cof = mCofWrapper.get(entityId).cof;
    int[] component = mCofComponents.get(entityId).component;
    AssetDescriptor<? extends DC>[] descriptors = mCofComponentDescriptors.get(entityId).descriptors;

    // data\global\monsters\FK\rh\FKRHFBLA11HS.dcc
    final int start = type.PATH.length() + 4; // start after token
    builder.setLength(0);
    builder
        .append(type.PATH).append('\\')
        .append(reference.token).append('\\')
        .append("AA").append('\\')
        .append(reference.token).append("AABBB").append(type.getMode(reference.mode)).append("CCC").append('.');
    for (int l = 0, size = cof.getNumLayers(); l < size; l++) {
      COF.Layer layer = cof.getLayer(l);
      int c = layer.component;
      if (!Dirty.isDirty(flags, c)) continue;
      if (component[c] == CofComponents.COMPONENT_NIL) {
        requiresReload |= (1 << c);
        unload(c, descriptors);
        continue;
      } else if (component[c] == CofComponents.COMPONENT_NULL) {
        component[c] = CofComponents.COMPONENT_LIT;
      }

      String composite = Engine.getComposite(c);
      builder
          .replace(start     , start +  2, composite)
          .replace(start +  5, start +  7, composite)
          .replace(start +  7, start + 10, type.COMP[component[c]])
          .replace(start + 12, start + 15, layer.weaponClass);

      unload(c, descriptors);
      AssetDescriptor<? extends DC> descriptor = descriptors[c];
      String path = builder.replace(start + 16, start + 19, DCC.EXT).toString();
      if (Riiablo.mpqs.contains(path)) {
        descriptor = descriptors[c] = new AssetDescriptor<>(path, DCC.class);
      } else {
        path = builder.replace(start + 16, start + 19, DC6.EXT).toString();
        assert Riiablo.mpqs.contains(path) : "Failed to locate " + path + " after looking for DCC and DC6";
        descriptor = descriptors[c] = new AssetDescriptor<>(path, DC6.class);
      }

      if (DEBUG) Gdx.app.log(TAG, "Loading[" + Engine.getComposite(c) + "] " + path);
      Riiablo.assets.load(descriptor);
      requiresReload |= (1 << c);
    }

    return requiresReload;
  }

  void unload(int c, AssetDescriptor[] descriptors) {
    AssetDescriptor descriptor = descriptors[c];
    if (descriptor == null) return;
    descriptors[c] = null;
    Riiablo.assets.unload(descriptor.fileName);
    if (DEBUG) Gdx.app.debug(TAG, "Unloading[" + Engine.getComposite(c) + "] " + descriptor.fileName);
  }
}
