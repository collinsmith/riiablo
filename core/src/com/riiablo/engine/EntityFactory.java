package com.riiablo.engine;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.CharData;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.Classname;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.item.Item;
import com.riiablo.map.DS1;
import com.riiablo.map.Map;

import net.mostlyoriginal.api.system.core.PassiveSystem;

public abstract class EntityFactory extends PassiveSystem {
  private static final String TAG = "EntityFactory";

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Classname> mClassname;
  protected ComponentMapper<MapWrapper> mMapWrapper;
  protected ComponentMapper<CofReference> mCofReference;

  protected final int createEntity(Class.Type type, String classname) {
    int id = world.create();
    mClass.create(id).type = type;
    mClassname.create(id).classname = classname;
    return id;
  }

  protected final int createEntity(Class.Type type, String classname, Map map, Map.Zone zone) {
    int id = createEntity(type, classname);
    mMapWrapper.create(id).set(map, zone);
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

  public abstract int createPlayer(Map map, Map.Zone zone, CharData charData, Vector2 position);
  public abstract int createDynamicObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y);
  public abstract int createStaticObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y);
  public abstract int createMonster(Map map, Map.Zone zone, MonStats.Entry monstats, float x, float y);
  public abstract int createWarp(Map map, Map.Zone zone, int index, float x, float y);
  public abstract int createItem(Item item, Vector2 position);
  public abstract int createMissile(Missiles.Entry missile, Vector2 angle, Vector2 position);
}
