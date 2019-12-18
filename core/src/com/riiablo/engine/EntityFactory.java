package com.riiablo.engine;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.CharData;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.Objects;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.ObjectInitializer;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.Classname;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.engine.server.component.Object;
import com.riiablo.item.Item;
import com.riiablo.map.DS1;
import com.riiablo.map.Map;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public abstract class EntityFactory extends PassiveSystem {
  private static final String TAG = "EntityFactory";

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Classname> mClassname;
  protected ComponentMapper<MapWrapper> mMapWrapper;
  protected ComponentMapper<Object> mObject;
  protected ComponentMapper<CofReference> mCofReference;

  protected CofManager cofs;
  protected ObjectInitializer objectInitializer;

  protected final int createEntity(Class.Type type, String classname) {
    int id = world.create();
    mClass.create(id).type = type;
    mClassname.create(id).classname = classname;
    return id;
  }

  protected final int createEntity(Class.Type type, String classname, Map map, Map.Zone zone) {
    int id = createEntity(type, classname);
    MapWrapper mapWrapper = mMapWrapper.create(id);
    mapWrapper.map = map;
    mapWrapper.zone = zone;
    return id;
  }

  public final int createPlayer(Map map, Map.Zone zone, CharData charData, Vector2 position) {
    int id = _createPlayer(map, zone, charData, position);
    if (id == Engine.INVALID_ENTITY) return id;
    cofs.setMode(id, Engine.Player.MODE_TN, true);
    cofs.setWClass(id, Engine.WEAPON_HTH, true);
    return id;
  }

  public final int createObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y) {
    switch (object.type) {
      case DS1.Object.DYNAMIC_TYPE:
        return createDynamicObject(map, zone, preset, object, x, y);
      case DS1.Object.STATIC_TYPE:
        return createStaticObject(map, zone, preset, object, x, y);
      default:
        Gdx.app.error(TAG, "Unexpected ds1 object type: " + object.type);
        return Engine.INVALID_ENTITY;
    }
  }

  public final int createDynamicObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y) {
    return _createDynamicObject(map, zone, preset, object, x, y);
  }

  public final int createStaticObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y) {
    int id = _createStaticObject(map, zone, preset, object, x, y);
    if (id == Engine.INVALID_ENTITY) return id;
    Objects.Entry base = mObject.get(id).base;
    if (base.Draw) {
      cofs.setMode(id, Engine.Object.MODE_NU, true);
      cofs.setWClass(id, Engine.WEAPON_HTH, true);
    }
    objectInitializer.initialize(id);
    return id;
  }

  public final int createMonster(Map map, Map.Zone zone, MonStats.Entry monstats, float x, float y) {
    int id = _createMonster(map, zone, monstats, x, y);
    if (id == Engine.INVALID_ENTITY) return id;
    CofReference reference = mCofReference.get(id);
    cofs.setMode(id, reference.mode, true);
    cofs.setWClass(id, reference.wclass, true);
    return id;
  }

  public final int createWarp(Map map, Map.Zone zone, int index, float x, float y) {
    return _createWarp(map, zone, index, x, y);
  }

  public final int createItem(Item item, Vector2 position) {
    return _createItem(item, position);
  }

  public final int createMissile(Missiles.Entry missile, Vector2 angle, Vector2 position) {
    return _createMissile(missile, angle, position);
  }

  protected abstract int _createPlayer(Map map, Map.Zone zone, CharData charData, Vector2 position);
  protected abstract int _createDynamicObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y);
  protected abstract int _createStaticObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y);
  protected abstract int _createMonster(Map map, Map.Zone zone, MonStats.Entry monstats, float x, float y);
  protected abstract int _createWarp(Map map, Map.Zone zone, int index, float x, float y);
  protected abstract int _createItem(Item item, Vector2 position);
  protected abstract int _createMissile(Missiles.Entry missile, Vector2 angle, Vector2 position);
}
