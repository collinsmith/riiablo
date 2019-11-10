package com.riiablo.engine.system;


import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.riiablo.codec.COF;
import com.riiablo.engine.Dirty;
import com.riiablo.engine.SystemPriority;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.TypeComponent;

public class CofSystem extends IteratingSystem {
  private static final String TAG = "CofSystem";

  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_COF   = DEBUG && true;

  private final ComponentMapper<TypeComponent> typeComponent = ComponentMapper.getFor(TypeComponent.class);
  private final ComponentMapper<CofComponent> cofComponent = ComponentMapper.getFor(CofComponent.class);

  public CofSystem() {
    super(Family.all(TypeComponent.class, CofComponent.class).get(), SystemPriority.CofSystem);
  }

  @Override
  protected void processEntity(Entity entity, float delta) {
    TypeComponent.Type type = typeComponent.get(entity).type;
    CofComponent cofComponent = this.cofComponent.get(entity);
    String cofName = generateCof(cofComponent, type);
    COF newCof = type.getCOFs().lookup(cofName);
    if (cofComponent.cof == newCof) return;

    cofComponent.cof = newCof;
    if (DEBUG_COF) Gdx.app.debug(TAG, cofName + "=" + cofComponent.cof);

    cofComponent.dirty = Dirty.ALL;
    cofComponent.load  = Dirty.NONE;
  }

  private static String generateCof(CofComponent c, TypeComponent.Type t) {
    return c.token + t.MODE[c.mode] + CofComponent.WCLASS[c.wclass];
  }
}