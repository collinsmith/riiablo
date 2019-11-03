package com.riiablo.engine;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;
import com.riiablo.codec.excel.Objects;
import com.riiablo.codec.util.BBox;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.BBoxComponent;
import com.riiablo.engine.component.ClassnameComponent;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.DS1Component;
import com.riiablo.engine.component.IdComponent;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.MonsterComponent;
import com.riiablo.engine.component.ObjectComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.TypeComponent;
import com.riiablo.engine.component.WarpComponent;
import com.riiablo.map.DS1;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;

import org.apache.commons.lang3.StringUtils;

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

  public static final class Monster {
    public static final byte MODE_DT =  0;
    public static final byte MODE_NU =  1;
    public static final byte MODE_WL =  2;
    public static final byte MODE_GH =  3;
    public static final byte MODE_A1 =  4;
    public static final byte MODE_A2 =  5;
    public static final byte MODE_BL =  6;
    public static final byte MODE_SC =  7;
    public static final byte MODE_S1 =  8;
    public static final byte MODE_S2 =  9;
    public static final byte MODE_S3 = 10;
    public static final byte MODE_S4 = 11;
    public static final byte MODE_DD = 12;
    //public static final byte MODE_GH = 13;
    public static final byte MODE_XX = 14;
    public static final byte MODE_RN = 15;
  }

  public Engine() {
    super();
  }

  @Override
  public Entity createEntity() {
    return createEntity(null);
  }

  public Entity createEntity(String classname) {
    ClassnameComponent classnameComponent = createComponent(ClassnameComponent.class);
    classnameComponent.classname = classname;

    Entity entity = super.createEntity();
    entity.add(createComponent(IdComponent.class));
    entity.add(classnameComponent);
    return entity;
  }

  public Entity createObject(Map map, Map.Zone zone, DS1 ds1, DS1.Object object, float x, float y) {
    final int type = object.type;
    switch (type) {
      case DS1.Object.DYNAMIC_TYPE:
        return createDynamicObject(map, zone, ds1, object, x, y);
      case DS1.Object.STATIC_TYPE:
        return createStaticObject(map, zone, ds1, object, x, y);
      default:
        Gdx.app.error(TAG, "Unexpected type: " + type);
        return null;
    }
  }

  private Entity createDynamicObject(Map map, Map.Zone zone, DS1 ds1, DS1.Object object, float x, float y) {
    assert object.type == DS1.Object.DYNAMIC_TYPE;
    return createMonster(map, zone, ds1, object, x, y);
  }

  private Entity createStaticObject(Map map, Map.Zone zone, DS1 ds1, DS1.Object object, float x, float y) {
    assert object.type == DS1.Object.STATIC_TYPE;
    int id = Riiablo.files.obj.getType2(ds1.getAct(), object.id);
    Objects.Entry base = Riiablo.files.objects.get(id);
    if (base == null) {
      Gdx.app.error(TAG, "Unknown static entity id: " + id + "; object=" + object);
      return null;
    }

    TypeComponent typeComponent = createComponent(TypeComponent.class);
    typeComponent.type = TypeComponent.Type.OBJ;

    CofComponent cofComponent = createComponent(CofComponent.class);
    cofComponent.token  = base.Token;
    cofComponent.mode   = Object.MODE_NU;
    cofComponent.wclass = CofComponent.WEAPON_HTH;
    Arrays.fill(cofComponent.component, CofComponent.COMPONENT_NULL);

    AnimationComponent animationComponent = createComponent(AnimationComponent.class);

    BBoxComponent boxComponent = createComponent(BBoxComponent.class);

    PositionComponent positionComponent = createComponent(PositionComponent.class);
    positionComponent.position.set(x, y);

    ObjectComponent objectComponent = createComponent(ObjectComponent.class);
    objectComponent.base = base;

    MapComponent mapComponent = createComponent(MapComponent.class);
    mapComponent.map = map;
    mapComponent.zone = zone;

    DS1Component ds1Component = createComponent(DS1Component.class);
    ds1Component.ds1 = ds1;
    ds1Component.object = object;

    Entity entity = createEntity(base.Description);
    entity.add(typeComponent);
    entity.add(cofComponent);
    entity.add(animationComponent);
    entity.add(boxComponent);
    entity.add(positionComponent);
    entity.add(mapComponent);
    entity.add(ds1Component);
    entity.add(objectComponent);

    // flags

    return entity;
  }

  private Entity createMonster(Map map, Map.Zone zone, DS1 ds1, DS1.Object object, float x, float y) {
    String id = Riiablo.files.obj.getType1(ds1.getAct(), object.id);
    MonStats.Entry monstats = Riiablo.files.monstats.get(id);
    if (monstats == null) {
      Gdx.app.error(TAG, "Unknown dynamic entity id: " + id + "; object=" + object);
      return null;
    }

    MonStats2.Entry monstats2 = Riiablo.files.monstats2.get(monstats.MonStatsEx);

    MonsterComponent monsterComponent = createComponent(MonsterComponent.class);
    monsterComponent.monstats = monstats;
    monsterComponent.monstats2 = monstats2;

    MapComponent mapComponent = createComponent(MapComponent.class);
    mapComponent.map = map;
    mapComponent.zone = zone;

    DS1Component ds1Component = createComponent(DS1Component.class);
    ds1Component.ds1 = ds1;
    ds1Component.object = object;

    TypeComponent typeComponent = createComponent(TypeComponent.class);
    typeComponent.type = TypeComponent.Type.MON;

    CofComponent cofComponent = createComponent(CofComponent.class);
    cofComponent.token  = monstats.Code;
    cofComponent.mode   = monstats.spawnmode.isEmpty() ? Monster.MODE_NU : (byte) Riiablo.files.MonMode.index(monstats.spawnmode);
    cofComponent.wclass = Riiablo.files.WeaponClass.index(monstats2.BaseW);
    for (byte i = 0; i < monstats2.ComponentV.length; i++) {
      String ComponentV = monstats2.ComponentV[i];
      if (!ComponentV.isEmpty()) {
        String[] v = StringUtils.remove(ComponentV, '"').split(",");
        int random = MathUtils.random(0, v.length - 1);
        cofComponent.component[i] = Riiablo.files.compcode.index(v[random]);
      }
    }

    AnimationComponent animationComponent = createComponent(AnimationComponent.class);

    BBoxComponent boxComponent = createComponent(BBoxComponent.class);

    PositionComponent positionComponent = createComponent(PositionComponent.class);
    positionComponent.position.set(x, y);

    Entity entity = createEntity(monstats.Id);
    entity.add(typeComponent);
    entity.add(cofComponent);
    entity.add(animationComponent);
    entity.add(boxComponent);
    entity.add(positionComponent);
    entity.add(mapComponent);
    entity.add(ds1Component);
    entity.add(monsterComponent);

    if (monstats2.isSel) entity.flags |= Flags.SELECTABLE;

    return entity;
  }

  public Entity createWarp(Map map, Map.Zone zone, int index, int x, int y) {
    final int mainIndex   = DT1.Tile.Index.mainIndex(index);
    final int subIndex    = DT1.Tile.Index.subIndex(index);
    final int orientation = DT1.Tile.Index.orientation(index);

    int dst = zone.level.Vis[mainIndex];
    assert dst > 0 : "Warp to unknown level!";
    int wrp = zone.level.Warp[mainIndex];
    assert wrp >= 0 : "Invalid warp";

    Levels.Entry dstLevel = Riiablo.files.Levels.get(dst);

    LvlWarp.Entry warp = Riiablo.files.LvlWarp.get(wrp);

    BBox box = new BBox();
    box.xMin = warp.SelectX;
    box.yMin = warp.SelectY;
    box.width = warp.SelectDX;
    box.height = warp.SelectDY;
    box.xMax = box.width + box.xMin;
    box.yMax = box.height + box.yMin;

    TypeComponent typeComponent = createComponent(TypeComponent.class);
    typeComponent.type = TypeComponent.Type.WRP;

    MapComponent mapComponent = createComponent(MapComponent.class);
    mapComponent.map = map;
    mapComponent.zone = zone;

    PositionComponent positionComponent = createComponent(PositionComponent.class);
    positionComponent.position.set(x, y).add(warp.OffsetX, warp.OffsetY);

    WarpComponent warpComponent = createComponent(WarpComponent.class);
    warpComponent.index = index;
    warpComponent.dstLevel = dstLevel;
    warpComponent.warp = warp;
    IntIntMap substs = warpComponent.substs;
    if (warp.LitVersion) {
      // FIXME: Below will cover overwhelming majority of cases -- need to solve act 5 ice cave case where 3 tiles are used
      //        I think this can be done by checking if there's a texture with the same id, else it's a floor warp
      if (subIndex < 2) {
        for (int i = 0; i < 2; i++) {
          substs.put(DT1.Tile.Index.create(orientation, mainIndex, i), DT1.Tile.Index.create(orientation, mainIndex, i + warp.Tiles));
        }
      } else {
        substs.put(DT1.Tile.Index.create(0, subIndex, 0), DT1.Tile.Index.create(0, subIndex, 4));
        substs.put(DT1.Tile.Index.create(0, subIndex, 1), DT1.Tile.Index.create(0, subIndex, 5));
        substs.put(DT1.Tile.Index.create(0, subIndex, 2), DT1.Tile.Index.create(0, subIndex, 6));
        substs.put(DT1.Tile.Index.create(0, subIndex, 3), DT1.Tile.Index.create(0, subIndex, 7));
      }
    } else {
      //substs = EMPTY_INT_INT_MAP;
    }

    BBoxComponent boxComponent = createComponent(BBoxComponent.class);
    boxComponent.box = box;

    Entity entity = createEntity("warp");
    entity.add(typeComponent);
    entity.add(mapComponent);
    entity.add(positionComponent);
    entity.add(warpComponent);
    entity.add(boxComponent);

    entity.flags |= Flags.SELECTABLE;

    return entity;
  }
}
