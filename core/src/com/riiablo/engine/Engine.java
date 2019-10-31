package com.riiablo.engine;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.excel.Objects;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.ClassnameComponent;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.IdComponent;
import com.riiablo.engine.component.ObjectComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.TypeComponent;
import com.riiablo.map.DS1;
import com.riiablo.map.Map;

import java.util.Arrays;

public class Engine extends PooledEngine {
  private static final String TAG = "Engine";

  public static final class Object {
    public static final int MODE_NU = 0;
    public static final int MODE_OP = 1;
    public static final int MODE_ON = 2;
    public static final int MODE_S1 = 3;
    public static final int MODE_S2 = 4;
    public static final int MODE_S3 = 5;
    public static final int MODE_S4 = 6;
    public static final int MODE_S5 = 7;

    static final int[] DEFAULT_COMPONENTS;
    static {
      DEFAULT_COMPONENTS = new int[COF.Component.NUM_COMPONENTS];
      Arrays.fill(DEFAULT_COMPONENTS, (byte) 1);
    }
  }

  public Engine() {
    super();
  }

  @Override
  public Entity createEntity() {
    Entity entity = super.createEntity();
    entity.add(createComponent(IdComponent.class));
    entity.add(createComponent(ClassnameComponent.class));
    return entity;
  }

  public Entity createObject(Map map, Map.Zone zone, DS1 ds1, DS1.Object object) {
    final int type = object.type;
    switch (type) {
      case DS1.Object.DYNAMIC_TYPE:
        return createDynamicObject(map, zone, ds1, object);
      case DS1.Object.STATIC_TYPE:
        return createStaticObject(map, zone, ds1, object);
      default:
        Gdx.app.error(TAG, "Unexpected type: " + type);
        return null;
    }
  }

  private Entity createDynamicObject(Map map, Map.Zone zone, DS1 ds1, DS1.Object object) {
    assert object.type == DS1.Object.DYNAMIC_TYPE;
    PositionComponent positionComponent = createComponent(PositionComponent.class);

    Entity entity = createEntity();
    entity.add(positionComponent);
    return entity;
  }

  private Entity createStaticObject(Map map, Map.Zone zone, DS1 ds1, DS1.Object object) {
    assert object.type == DS1.Object.STATIC_TYPE;
    int id = Riiablo.files.obj.getType2(ds1.getAct(), object.id);
    Objects.Entry base = Riiablo.files.objects.get(id);
    if (base == null) {
      Gdx.app.error(TAG, "Unknown static entity id: " + id + "; object=" + object);
      return null;
    }
    //if (!base.Draw) return null;

    TypeComponent typeComponent = createComponent(TypeComponent.class);
    typeComponent.type = TypeComponent.Type.OBJ;

    CofComponent cofComponent = createComponent(CofComponent.class);
    cofComponent.token  = base.Token;
    cofComponent.mode   = Object.MODE_NU;
    cofComponent.wclass = CofComponent.WEAPON_HTH;
    Arrays.fill(cofComponent.component, CofComponent.COMPONENT_NULL);

    AnimationComponent animationComponent = createComponent(AnimationComponent.class);

    PositionComponent positionComponent = createComponent(PositionComponent.class);

    ObjectComponent objectComponent = createComponent(ObjectComponent.class);
    objectComponent.base = base;

    Entity entity = createEntity();
    entity.add(typeComponent);
    entity.add(cofComponent);
    entity.add(animationComponent);
    entity.add(positionComponent);
    entity.add(objectComponent);
    entity.getComponent(ClassnameComponent.class).classname = base.Description;
    return entity;
  }
}
