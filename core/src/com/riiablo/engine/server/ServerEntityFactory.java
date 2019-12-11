package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.CharData;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.Objects;
import com.riiablo.engine.Engine;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.Classname;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.DS1ObjectWrapper;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.engine.server.component.Object;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Running;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.item.Item;
import com.riiablo.map.DS1;
import com.riiablo.map.Map;

import java.util.Arrays;

public class ServerEntityFactory extends EntityFactory {
  private static final String TAG = "ServerEntityFactory";

  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Classname> mClassname;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<Player> mPlayer;
  protected ComponentMapper<Object> mObject;
  protected ComponentMapper<Running> mRunning;
  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<MapWrapper> mMapWrapper;
  protected ComponentMapper<DS1ObjectWrapper> mDS1ObjectWrapper;

  protected CofManager cofs;
  protected ObjectInitializer objectInitializer;

  public int createPlayer(Map map, Map.Zone zone, CharData charData, Vector2 position) {
    int id = super.createEntity(Class.Type.PLR, "player", map, zone);
    mPlayer.create(id).data = charData;

    mPosition.create(id).position.set(position);
    mVelocity.create(id);
    mAngle.create(id);

    mCofReference.create(id).token = Engine.Player.getToken(charData.getD2S().header.charClass);
    mCofComponents.create(id);
    mCofAlphas.create(id);
    mCofTransforms.create(id);

    mRunning.create(id);
    mNetworked.create(id);

    MapWrapper mapWrapper = mMapWrapper.create(id);
    mapWrapper.map = map;
    mapWrapper.zone = zone;

    cofs.setMode(id, Engine.Player.MODE_TN, true);
    cofs.setWClass(id, Engine.WEAPON_HTH, true);
    return id;
  }

  @Override
  public int createDynamicObject(Map map, Map.Zone zone, DS1 ds1, DS1.Object object, float x, float y) {
    return Engine.INVALID_ENTITY;
  }

  @Override
  public int createStaticObject(Map map, Map.Zone zone, DS1 ds1, DS1.Object object, float x, float y) {
    assert object.type == DS1.Object.STATIC_TYPE;
    int objectType = Riiablo.files.obj.getType2(ds1.getAct(), object.id);
    Objects.Entry base = Riiablo.files.objects.get(objectType);
    if (base == null) {
      Gdx.app.error(TAG, "Unknown static entity id: " + objectType + "; object=" + object);
      return Engine.INVALID_ENTITY;
    }

    String name;
    if ((base.SubClass & Engine.Object.SUBCLASS_WAYPOINT) == Engine.Object.SUBCLASS_WAYPOINT) {
      String levelName = Riiablo.string.lookup(zone.level.LevelName);
      String objectName = Riiablo.string.lookup(base.Name);
      name = String.format("%s\n%s", levelName, objectName);
    } else {
      name = base.Name.equalsIgnoreCase("dummy") ? base.Description : Riiablo.string.lookup(base.Name);
    }

    int id = world.create();

    mClass.create(id).type = Class.Type.OBJ;
    mClassname.create(id).classname = base.Description;
    mObject.create(id).base = base;

    mPosition.create(id).position.set(x, y);

    if (base.Draw) {
      mCofReference.create(id).token = base.Token;
      int[] component = mCofComponents.create(id).component;
      Arrays.fill(component, CofComponents.COMPONENT_NULL);
      mCofAlphas.create(id);
      mCofTransforms.create(id);
    }

    DS1ObjectWrapper ds1ObjectWrapper = mDS1ObjectWrapper.create(id);
    ds1ObjectWrapper.ds1 = ds1;
    ds1ObjectWrapper.object = object;

    if (base.Draw) {
      cofs.setMode(id, Engine.Object.MODE_NU, true);
      cofs.setWClass(id, Engine.WEAPON_HTH, true);
    }

    objectInitializer.initialize(id);
    return id;
  }

  @Override
  public int createMonster(Map map, Map.Zone zone, MonStats.Entry monstats, float x, float y) {
    return Engine.INVALID_ENTITY;
  }

  @Override
  public int createWarp(Map map, Map.Zone zone, int index, float x, float y) {
    return Engine.INVALID_ENTITY;
  }

  @Override
  public int createItem(Item item, Vector2 position) {
    return Engine.INVALID_ENTITY;
  }

  @Override
  public int createMissile(Missiles.Entry missile, Vector2 angle, Vector2 position) {
    return Engine.INVALID_ENTITY;
  }
}
