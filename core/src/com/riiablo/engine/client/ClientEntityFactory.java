package com.riiablo.engine.client;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.CharData;
import com.riiablo.Riiablo;
import com.riiablo.ai.AI;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;
import com.riiablo.codec.excel.Objects;
import com.riiablo.codec.util.BBox;
import com.riiablo.engine.Engine;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.client.component.BBoxWrapper;
import com.riiablo.engine.client.component.CofComponentDescriptors;
import com.riiablo.engine.client.component.Label;
import com.riiablo.engine.client.component.Selectable;
import com.riiablo.engine.server.CofManager;
import com.riiablo.engine.server.ItemInteractor;
import com.riiablo.engine.server.ObjectInitializer;
import com.riiablo.engine.server.ObjectInteractor;
import com.riiablo.engine.server.WarpInteractor;
import com.riiablo.engine.server.component.AIWrapper;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.DS1ObjectWrapper;
import com.riiablo.engine.server.component.Interactable;
import com.riiablo.engine.server.component.Missile;
import com.riiablo.engine.server.component.Monster;
import com.riiablo.engine.server.component.MovementModes;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.engine.server.component.Object;
import com.riiablo.engine.server.component.PathWrapper;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Running;
import com.riiablo.engine.server.component.Size;
import com.riiablo.engine.server.component.SoundEmitter;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.engine.server.component.Warp;
import com.riiablo.engine.server.component.ZoneAware;
import com.riiablo.item.Item;
import com.riiablo.map.DS1;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class ClientEntityFactory extends EntityFactory {
  private static final String TAG = "ClientEntityFactory";

  protected ComponentMapper<Player> mPlayer;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofComponentDescriptors> mCofComponentDescriptors;
  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;
  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<BBoxWrapper> mBBoxWrapper;
  protected ComponentMapper<Label> mLabel;
  protected ComponentMapper<DS1ObjectWrapper> mDS1ObjectWrapper;
  protected ComponentMapper<Running> mRunning;
  protected ComponentMapper<Object> mObject;
  protected ComponentMapper<Monster> mMonster;
  protected ComponentMapper<PathWrapper> mPathWrapper;
  protected ComponentMapper<MovementModes> mMovementModes;
  protected ComponentMapper<Selectable> mSelectable;
  protected ComponentMapper<Warp> mWarp;
  protected ComponentMapper<Interactable> mInteractable;
  protected ComponentMapper<Size> mSize;
  protected ComponentMapper<AIWrapper> mAIWrapper;
  protected ComponentMapper<com.riiablo.engine.server.component.Item> mItem;
  protected ComponentMapper<Missile> mMissile;
  protected ComponentMapper<SoundEmitter> mSoundEmitter;
  protected ComponentMapper<ZoneAware> mZoneAware;
  protected ComponentMapper<Box2DBody> mBox2DBody;

  protected CofManager cofs;
  protected ObjectInitializer objectInitializer;
  protected ObjectInteractor objectInteractor;
  protected WarpInteractor warpInteractor;
  protected ItemInteractor itemInteractor;

  @Override
  public int createPlayer(Map map, Map.Zone zone, CharData charData, Vector2 position) {
    int id = super.createEntity(Class.Type.PLR, "player", map, zone);
    mPlayer.create(id).data = charData;

    mPosition.create(id).position.set(position);
    mVelocity.create(id).set(6, 9);
    mAngle.create(id);

    mCofReference.create(id).token = Engine.Player.getToken(charData.getD2S().header.charClass);
    mCofComponents.create(id);
    mCofComponentDescriptors.create(id);
    mCofAlphas.create(id);
    mCofTransforms.create(id);

    mMovementModes.create(id).set(Engine.Player.MODE_TN, Engine.Player.MODE_TW, Engine.Player.MODE_RN);

    mAnimationWrapper.create(id);
    mBBoxWrapper.create(id).box = mAnimationWrapper.get(id).animation.getBox();

    mSize.create(id).size = Size.MEDIUM;
    mBox2DBody.create(id);

    mRunning.create(id);
    mNetworked.create(id);
    mZoneAware.create(id);

    cofs.setMode(id, Engine.Player.MODE_TN, true);
    cofs.setWClass(id, Engine.WEAPON_HTH, true);
    return id;
  }

  @Override
  public int createDynamicObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y) {
    String objectType = Riiablo.files.obj.getType1(preset.getDS1().getAct(), object.id);
    MonStats.Entry monstats = Riiablo.files.monstats.get(objectType);
    if (monstats == null) {
      Gdx.app.error(TAG, "Unknown dynamic entity id: " + objectType + "; " + preset + "; object=" + object);
      return Engine.INVALID_ENTITY;
    }

    int id = createMonster(map, zone, monstats, x, y);

    DS1ObjectWrapper ds1ObjectWrapper = mDS1ObjectWrapper.create(id);
    ds1ObjectWrapper.ds1 = preset.getDS1();
    ds1ObjectWrapper.object = object;

    if (object != null && object.path != null) {
      mPathWrapper.create(id).path = object.path;
    }

    return id;
  }

  @Override
  public int createStaticObject(Map map, Map.Zone zone, Map.Preset preset, DS1.Object object, float x, float y) {
    assert object.type == DS1.Object.STATIC_TYPE;
    int objectType = Riiablo.files.obj.getType2(preset.getDS1().getAct(), object.id);
    Objects.Entry base = Riiablo.files.objects.get(objectType);
    if (base == null) {
      Gdx.app.error(TAG, "Unknown static entity id: " + objectType + "; " + preset + "; object=" + object);
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

    int id = super.createEntity(Class.Type.OBJ, base.Description, map, zone);
    mObject.create(id).base = base;

    mPosition.create(id).position.set(x, y);

    if (base.Draw) {
      mCofReference.create(id).token = base.Token;
      int[] component = mCofComponents.create(id).component;
      Arrays.fill(component, CofComponents.COMPONENT_NULL);
      mCofComponentDescriptors.create(id);
      mCofAlphas.create(id);
      mCofTransforms.create(id);

      mAnimationWrapper.create(id);
    }

    BBoxWrapper boxWrapper = mBBoxWrapper.create(id);
    if ((base.SubClass & Engine.Object.SUBCLASS_WAYPOINT) == Engine.Object.SUBCLASS_WAYPOINT) {
      BBox box = boxWrapper.box = new BBox();
      box.xMin = -70;
      box.yMin = -30;
      box.xMax = -box.xMin;
      box.yMax = -box.yMin;
      box.width = Math.abs(2 * box.xMin);
      box.height = Math.abs(2 * box.yMin);
    } else if (mAnimationWrapper.has(id)) {
      boxWrapper.box = mAnimationWrapper.get(id).animation.getBox();
    }

    Label label = mLabel.create(id);
    label.offset.y = -base.NameOffset;
    label.actor = createLabel(name);
    label.actor.setUserObject(id);

    DS1ObjectWrapper ds1ObjectWrapper = mDS1ObjectWrapper.create(id);
    ds1ObjectWrapper.ds1 = preset.getDS1();
    ds1ObjectWrapper.object = object;

    if (base.OperateRange > 0 && ArrayUtils.contains(base.Selectable, true)) {
      mInteractable.create(id).set(base.OperateRange, objectInteractor);
    }

    mSize.create(id); // single size doesn't make any sense in this case because this is a rect
    mBox2DBody.create(id);

    if (base.Draw) {
      cofs.setMode(id, Engine.Object.MODE_NU, true);
      cofs.setWClass(id, Engine.WEAPON_HTH, true);
    }

    objectInitializer.initialize(id);
    return id;
  }

  @Override
  public int createMonster(Map map, Map.Zone zone, MonStats.Entry monstats, float x, float y) {
    MonStats2.Entry monstats2 = Riiablo.files.monstats2.get(monstats.MonStatsEx);

    String name = monstats.NameStr.equalsIgnoreCase("dummy")
        ? monstats.Id : Riiablo.string.lookup(monstats.NameStr);

    int id = super.createEntity(Class.Type.MON, monstats.Id, map, zone);
    mMonster.create(id).set(monstats, monstats2);

    mPosition.create(id).position.set(x, y);
    mVelocity.create(id).set(monstats.Velocity, monstats.Run);
    mAngle.create(id);

    CofReference reference = mCofReference.create(id);
    reference.token  = monstats.Code;
    reference.mode   = monstats.spawnmode.isEmpty() ? Engine.Monster.MODE_NU : (byte) Riiablo.files.MonMode.index(monstats.spawnmode);
    reference.wclass = (byte) Riiablo.files.WeaponClass.index(monstats2.BaseW);
    int[] component = mCofComponents.create(id).component;
    for (byte i = 0; i < monstats2.ComponentV.length; i++) {
      String ComponentV = monstats2.ComponentV[i];
      if (!ComponentV.isEmpty()) {
        String[] v = StringUtils.remove(ComponentV, '"').split(",");
        int random = MathUtils.random(0, v.length - 1);
        component[i] = Riiablo.files.compcode.index(v[random]);
      }
    }

    mCofComponentDescriptors.create(id);
    mCofAlphas.create(id);
    mCofTransforms.create(id);

    mMovementModes.create(id).set(Engine.Monster.MODE_NU, Engine.Monster.MODE_WL, Engine.Monster.MODE_RN);

    mAnimationWrapper.create(id);
    mBBoxWrapper.create(id).box = mAnimationWrapper.get(id).animation.getBox();

    if (monstats.Align == 1) {
      Label label = mLabel.create(id);
      label.offset.y = monstats2.pixHeight;
      label.actor = createLabel(name);
      label.actor.setUserObject(id);
    }

    if (monstats2.isSel) mSelectable.create(id);

    float size = mSize.create(id).size = monstats2.SizeX; // FIXME: SizeX and SizeY appear to always be equal -- is this method sufficient?
    mBox2DBody.create(id);
    AI ai = mAIWrapper.create(id).findAI(id, monstats.AI).ai;
    world.getInjector().inject(ai);
    ai.initialize();
    if (monstats.interact) {
      mInteractable.create(id).set(size, ai);
    }

    cofs.setMode(id, reference.mode, true);
    cofs.setWClass(id, reference.wclass, true);
    return id;
  }

  @Override
  public int createWarp(Map map, Map.Zone zone, int index, float x, float y) {
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

    int id = super.createEntity(Class.Type.WRP, "warp", map, zone);
    IntIntMap substs = mWarp.create(id).set(index, warp, dstLevel).substs;
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
    }

    mPosition.create(id).position.set(x, y).add(warp.OffsetX, warp.OffsetY);

    BBoxWrapper boxWrapper = mBBoxWrapper.create(id);
    boxWrapper.box = box;

    Label label = mLabel.create(id);
    label.offset.set(box.xMin + box.width / 2, -box.yMax + box.height / 2);
    label.actor = createLabel(name);
    label.actor.setUserObject(id);

    mSelectable.create(id);
    mInteractable.create(id).set(3.0f, warpInteractor);

    return id;
  }

  @Override
  public int createItem(Item item, Vector2 position) {
    int id = super.createEntity(Class.Type.ITM, "item");
    com.riiablo.engine.server.component.Item itemWrapper = mItem.create(id).set(item);
    Riiablo.assets.load(itemWrapper.flippyDescriptor);

    /**
     * FIXME: at least some items appear to be about a half subtile too high after their drop
     *        animations finish -- is this expected? or some issue with offsets? It's happening with
     *        runes -- but keys are placed correctly and other items I've tried look fine.
     */
    mPosition.create(id).position.set(position);
    mInteractable.create(id).set(1f, itemInteractor);
    return id;
  }

  @Override
  public int createMissile(Missiles.Entry missile, Vector2 angle, Vector2 position) {
    int id = super.createEntity(Class.Type.MIS, missile.Missile);
    Missile missileWrapper = mMissile.create(id).set(missile, position, missile.Range);
    Riiablo.assets.load(missileWrapper.missileDescriptor);

    mPosition.create(id).position.set(position);
    mVelocity.create(id).velocity.set(angle).setLength(missile.Vel);
    mAngle.create(id).set(angle);

    if (!missile.TravelSound.isEmpty()) {
      mSoundEmitter.create(id).set(Riiablo.audio.play(missile.TravelSound, true), Interpolation.pow2OutInverse);
    }

    return id;
  }

  private static com.riiablo.widget.Label createLabel(String text) {
    com.riiablo.widget.Label label = new com.riiablo.widget.Label(Riiablo.fonts.font16);
    label.setAlignment(Align.center);
    label.getStyle().background = com.riiablo.widget.Label.MODAL;
    label.setText(text);
    return label;
  }
}
