package com.riiablo.engine;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.CharData;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.DC6;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;
import com.riiablo.codec.excel.Objects;
import com.riiablo.codec.util.BBox;
import com.riiablo.engine.component.AngleComponent;
import com.riiablo.engine.component.AnimationComponent;
import com.riiablo.engine.component.BBoxComponent;
import com.riiablo.engine.component.Box2DComponent;
import com.riiablo.engine.component.ClassnameComponent;
import com.riiablo.engine.component.CofComponent;
import com.riiablo.engine.component.DS1Component;
import com.riiablo.engine.component.IdComponent;
import com.riiablo.engine.component.InteractableComponent;
import com.riiablo.engine.component.ItemComponent;
import com.riiablo.engine.component.LabelComponent;
import com.riiablo.engine.component.MapComponent;
import com.riiablo.engine.component.MonsterComponent;
import com.riiablo.engine.component.MovementModeComponent;
import com.riiablo.engine.component.ObjectComponent;
import com.riiablo.engine.component.PathComponent;
import com.riiablo.engine.component.PlayerComponent;
import com.riiablo.engine.component.PositionComponent;
import com.riiablo.engine.component.SizeComponent;
import com.riiablo.engine.component.TypeComponent;
import com.riiablo.engine.component.VelocityComponent;
import com.riiablo.engine.component.WarpComponent;
import com.riiablo.engine.component.ZoneAwareComponent;
import com.riiablo.item.Item;
import com.riiablo.map.DS1;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;
import com.riiablo.widget.Label;

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

  public static final class Player {
    public static final byte MODE_DT =  0;
    public static final byte MODE_NU =  1;
    public static final byte MODE_WL =  2;
    public static final byte MODE_RN =  3;
    public static final byte MODE_GH =  4;
    public static final byte MODE_TN =  5;
    public static final byte MODE_TW =  6;
    public static final byte MODE_A1 =  7;
    public static final byte MODE_A2 =  8;
    public static final byte MODE_BL =  9;
    public static final byte MODE_SC = 10;
    public static final byte MODE_TH = 11;
    public static final byte MODE_KK = 12;
    public static final byte MODE_S1 = 13;
    public static final byte MODE_S2 = 14;
    public static final byte MODE_S3 = 15;
    public static final byte MODE_S4 = 16;
    public static final byte MODE_DD = 17;
    //public static final byte MODE_GH = 18;
    //public static final byte MODE_GH = 19;
  }

  private static Label createLabel(String text) {
    Label label = new Label(Riiablo.fonts.font16);
    label.setAlignment(Align.center);
    label.getStyle().background = Label.MODAL;
    label.setText(text);
    return label;
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
    final Objects.Entry base = Riiablo.files.objects.get(id);
    if (base == null) {
      Gdx.app.error(TAG, "Unknown static entity id: " + id + "; object=" + object);
      return null;
    }

    String name;
    if (base.SubClass == 64) {
      String levelName = Riiablo.string.lookup(zone.level.LevelName);
      name = String.format("%s\n%s", levelName, Riiablo.string.lookup(base.Name));
    } else {
      name = base.Name.equalsIgnoreCase("dummy") ? base.Description : Riiablo.string.lookup(base.Name);
    }

    boolean draw = base.Draw;

    TypeComponent typeComponent = createComponent(TypeComponent.class);
    typeComponent.type = TypeComponent.Type.OBJ;

    CofComponent cofComponent;
    if (draw) {
      cofComponent = createComponent(CofComponent.class);
      cofComponent.token = base.Token;
      cofComponent.mode = Object.MODE_NU;
      cofComponent.wclass = CofComponent.WEAPON_HTH;
      Arrays.fill(cofComponent.component, CofComponent.COMPONENT_NULL);
    } else {
      cofComponent = null;
    }

    AnimationComponent animationComponent;
    if (draw) {
      animationComponent = createComponent(AnimationComponent.class);
    } else {
      animationComponent = null;
    }

    BBoxComponent boxComponent = createComponent(BBoxComponent.class);
    if (base.SubClass == 64) {
      BBox box = boxComponent.box = new BBox();
      box.xMin = -70;
      box.yMin = -30;
      box.xMax = -box.xMin;
      box.yMax = -box.yMin;
      box.width = Math.abs(2 * box.xMin);
      box.height = Math.abs(2 * box.yMin);
    } else if (animationComponent != null) {
      boxComponent.box = animationComponent.animation.getBox();
    }

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

    LabelComponent labelComponent = createComponent(LabelComponent.class);
    labelComponent.offset.y = -base.NameOffset;
    labelComponent.actor = createLabel(name);

    InteractableComponent interactableComponent = null;
    if (base.OperateRange > 0) {
      interactableComponent = createComponent(InteractableComponent.class);
      interactableComponent.range = base.OperateRange;
    }

    SizeComponent sizeComponent = createComponent(SizeComponent.class);

    Box2DComponent box2DComponent = createComponent(Box2DComponent.class);

    Entity entity = createEntity(base.Description);
    entity.add(typeComponent);
    if (draw) entity.add(cofComponent);
    if (draw) entity.add(animationComponent);
    entity.add(boxComponent);
    entity.add(positionComponent);
    entity.add(mapComponent);
    entity.add(ds1Component);
    entity.add(objectComponent);
    entity.add(labelComponent);
    if (interactableComponent != null) entity.add(interactableComponent);
    entity.add(sizeComponent);
    entity.add(box2DComponent);

    labelComponent.actor.setUserObject(entity);

    if (!draw) entity.flags |= Flags.INVISIBLE;

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

    String name = monstats.NameStr.equalsIgnoreCase("dummy") ? monstats.Id : Riiablo.string.lookup(monstats.NameStr);

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
    boxComponent.box = animationComponent.animation.getBox();

    PositionComponent positionComponent = createComponent(PositionComponent.class);
    positionComponent.position.set(x, y);

    VelocityComponent velocityComponent = createComponent(VelocityComponent.class);
    velocityComponent.walkSpeed = monstats.Velocity;
    velocityComponent.runSpeed = monstats.Run;

    LabelComponent labelComponent = createComponent(LabelComponent.class);
    labelComponent.offset.y = monstats2.pixHeight;
    labelComponent.actor = createLabel(name);

    InteractableComponent interactableComponent = null;
    if (monstats.interact) {
      interactableComponent = createComponent(InteractableComponent.class);
      interactableComponent.range = monstats2.SizeX;
      // FIXME: SizeX and SizeY appear to always be equal -- is this method sufficient?
    }

    SizeComponent sizeComponent = createComponent(SizeComponent.class);
    sizeComponent.size = monstats2.SizeX; // FIXME: see above note

    Box2DComponent box2DComponent = createComponent(Box2DComponent.class);

    Entity entity = createEntity(monstats.Id);
    entity.add(typeComponent);
    entity.add(cofComponent);
    entity.add(animationComponent);
    entity.add(boxComponent);
    entity.add(positionComponent);
    entity.add(velocityComponent);
    entity.add(mapComponent);
    entity.add(ds1Component);
    entity.add(monsterComponent);
    entity.add(labelComponent);
    if (interactableComponent != null) entity.add(interactableComponent);
    entity.add(sizeComponent);
    entity.add(box2DComponent);

    labelComponent.actor.setUserObject(entity);

    if (object != null && object.path != null) {
      PathComponent pathComponent = createComponent(PathComponent.class);
      pathComponent.path = object.path;
      entity.add(pathComponent);
    }

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
    String name = Riiablo.string.lookup(dstLevel.LevelWarp);

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

    LabelComponent labelComponent = createComponent(LabelComponent.class);
    labelComponent.offset.set(box.xMin + box.width / 2, -box.yMax + box.height / 2);
    labelComponent.actor = createLabel(name);

    InteractableComponent interactableComponent = createComponent(InteractableComponent.class);
    interactableComponent.range = 3.0f;

    Entity entity = createEntity("warp");
    entity.add(typeComponent);
    entity.add(mapComponent);
    entity.add(positionComponent);
    entity.add(warpComponent);
    entity.add(boxComponent);
    entity.add(labelComponent);
    entity.add(interactableComponent);

    labelComponent.actor.setUserObject(entity);

    return entity;
  }

  public Entity createPlayer(Map map, Map.Zone zone, CharData charData, float x, float y) {
    TypeComponent typeComponent = createComponent(TypeComponent.class);
    typeComponent.type = TypeComponent.Type.PLR;

    PlayerComponent playerComponent = createComponent(PlayerComponent.class);
    playerComponent.charData = charData;

    CofComponent cofComponent = createComponent(CofComponent.class);
    cofComponent.mode = Player.MODE_TN;

    AnimationComponent animationComponent = createComponent(AnimationComponent.class);

    BBoxComponent boxComponent = createComponent(BBoxComponent.class);
    boxComponent.box = animationComponent.animation.getBox();

    PositionComponent positionComponent = createComponent(PositionComponent.class);
    positionComponent.position.set(x, y);

    VelocityComponent velocityComponent = createComponent(VelocityComponent.class);
    velocityComponent.walkSpeed = 6;
    velocityComponent.runSpeed = 9;

    MovementModeComponent movementModeComponent = createComponent(MovementModeComponent.class);
    movementModeComponent.NU = Player.MODE_TN;
    movementModeComponent.WL = Player.MODE_TW;
    movementModeComponent.RN = Player.MODE_RN;

    ZoneAwareComponent zoneAwareComponent = createComponent(ZoneAwareComponent.class);

    AngleComponent angleComponent = createComponent(AngleComponent.class);

    MapComponent mapComponent = createComponent(MapComponent.class);
    mapComponent.map = map;
    mapComponent.zone = zone;

    SizeComponent sizeComponent = createComponent(SizeComponent.class);
    sizeComponent.size = SizeComponent.MEDIUM;

    Box2DComponent box2DComponent = createComponent(Box2DComponent.class);

    Entity entity = createEntity("player");
    entity.add(typeComponent);
    entity.add(cofComponent);
    entity.add(animationComponent);
    entity.add(boxComponent);
    entity.add(positionComponent);
    entity.add(velocityComponent);
    entity.add(movementModeComponent);
    entity.add(angleComponent);
    entity.add(mapComponent);
    entity.add(playerComponent);
    entity.add(zoneAwareComponent);
    entity.add(sizeComponent);
    entity.add(box2DComponent);

    return entity;
  }

  public Entity createItem(Item item, Vector2 position) {
    TypeComponent typeComponent = createComponent(TypeComponent.class);
    typeComponent.type = TypeComponent.Type.ITM;

    AssetDescriptor<DC6> flippyDescriptor = new AssetDescriptor<>(TypeComponent.Type.ITM.PATH + "\\" + item.getFlippyFile() + ".dc6", DC6.class);
    Riiablo.assets.load(flippyDescriptor);

    ItemComponent itemComponent = createComponent(ItemComponent.class);
    itemComponent.item = item;
    itemComponent.flippyDescriptor = flippyDescriptor;

    PositionComponent positionComponent = createComponent(PositionComponent.class);
    positionComponent.position.set(position);

    InteractableComponent interactableComponent = createComponent(InteractableComponent.class);
    interactableComponent.range = 1f;

    Entity entity = createEntity("item");
    entity.add(typeComponent);
    entity.add(itemComponent);
    entity.add(positionComponent);
    entity.add(interactableComponent);

    return entity;
  }

  public static <T extends Component> T getOrCreateComponent(Entity entity, com.badlogic.ashley.core.Engine engine, Class<T> componentType, ComponentMapper<T> componentMapper) {
    T instance = componentMapper.get(entity);
    if (instance == null) entity.add(instance = engine.createComponent(componentType));
    return instance;
  }
}
