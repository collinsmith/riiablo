package com.riiablo.engine.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC;
import com.riiablo.codec.DCC;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.SystemPriority;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.TypeComponent;

@DependsOn(CofSystem.class)
public class CofLoaderSystem extends IteratingSystem {
  private static final String TAG = "CofLoaderSystem";

  private static final boolean DEBUG       = !true;
  private static final boolean DEBUG_DIRTY = DEBUG && true;

  private static final String[] COMPOSITE = {
      "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8",
  };

  private final ComponentMapper<TypeComponent> typeComponent = ComponentMapper.getFor(TypeComponent.class);
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);

  private final StringBuilder builder = new StringBuilder(64);

  public CofLoaderSystem() {
    super(Family.all(TypeComponent.class, CofComponent.class).get(), SystemPriority.CofLoaderSystem);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    TypeComponent.Type type = typeComponent.get(entity).type;
    CofComponent cofComponent = this.cofComponent.get(entity);
    if (cofComponent.dirty == Dirty.NONE) return;
    if (DEBUG_DIRTY) Gdx.app.debug(TAG, "dirty layers: " + Dirty.toString(cofComponent.dirty));

    // data\global\monsters\FK\rh\FKRHFBLA11HS.dcc
    final int start = type.PATH.length() + 4; // start after token
    builder.setLength(0);
    builder
        .append(type.PATH).append('\\')
        .append(cofComponent.token).append('\\')
        .append("AA").append("\\")
        .append(cofComponent.token).append("AABBB").append(type.MODE[cofComponent.mode]).append("CCC").append(".dcc");
    for (int l = 0, numLayers = cofComponent.cof.getNumLayers(); l < numLayers; l++) {
      COF.Layer layer = cofComponent.cof.getLayer(l);
      if (!Dirty.isDirty(cofComponent.dirty, layer.component)) continue;
      // TODO: should also ignore COMPONENT_NULL? used to set default components
      if (cofComponent.component[layer.component] == CofComponent.COMPONENT_NIL) {
        cofComponent.layer[layer.component] = null; // TODO: unload existing asset?
        cofComponent.load |= (1 << layer.component);
        continue;
      } else if (cofComponent.component[layer.component] == CofComponent.COMPONENT_NULL) {
        cofComponent.component[layer.component] = CofComponent.COMPONENT_LIT;
      }

      String composite = COMPOSITE[layer.component];
      builder
          .replace(start     , start +  2, composite)
          .replace(start +  5, start +  7, composite)
          .replace(start +  7, start + 10, type.COMP[cofComponent.component[layer.component]])
          .replace(start + 12, start + 15, layer.weaponClass);
      String path = builder.toString();
      if (DEBUG_DIRTY) Gdx.app.log(TAG, path);

      // TODO: unload existing asset?
      AssetDescriptor<? extends DC> descriptor = cofComponent.layer[layer.component] = new AssetDescriptor<>(path, DCC.class);
      Riiablo.assets.load(descriptor);
      cofComponent.load |= (1 << layer.component);
    }

    cofComponent.dirty = Dirty.NONE;
  }
}