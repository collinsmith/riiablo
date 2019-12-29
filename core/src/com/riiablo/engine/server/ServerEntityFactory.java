package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.riiablo.CharData;
import com.riiablo.Riiablo;
import com.riiablo.ai.AI;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;
import com.riiablo.codec.excel.Missiles;
import com.riiablo.codec.excel.MonStats;
import com.riiablo.codec.excel.MonStats2;
import com.riiablo.codec.excel.Objects;
import com.riiablo.engine.Engine;
import com.riiablo.engine.EntityFactory;
import com.riiablo.engine.server.component.AIWrapper;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.CofAlphas;
import com.riiablo.engine.server.component.CofComponents;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.CofTransforms;
import com.riiablo.engine.server.component.Interactable;
import com.riiablo.engine.server.component.Item;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.engine.server.component.Missile;
import com.riiablo.engine.server.component.Monster;
import com.riiablo.engine.server.component.MovementModes;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.engine.server.component.Object;
import com.riiablo.engine.server.component.Player;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Running;
import com.riiablo.engine.server.component.Size;
import com.riiablo.engine.server.component.Velocity;
import com.riiablo.engine.server.component.Warp;
import com.riiablo.engine.server.component.ZoneAware;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class ServerEntityFactory extends EntityFactory {
  private static final String TAG = "ServerEntityFactory";

  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Velocity> mVelocity;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<CofComponents> mCofComponents;
  protected ComponentMapper<CofAlphas> mCofAlphas;
  protected ComponentMapper<CofTransforms> mCofTransforms;
  protected ComponentMapper<Player> mPlayer;
  protected ComponentMapper<Object> mObject;
  protected ComponentMapper<Running> mRunning;
  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<MovementModes> mMovementModes;
  protected ComponentMapper<Size> mSize;
  protected ComponentMapper<ZoneAware> mZoneAware;
  protected ComponentMapper<Interactable> mInteractable;
  protected ComponentMapper<Monster> mMonster;
  protected ComponentMapper<Warp> mWarp;
  protected ComponentMapper<Item> mItem;
  protected ComponentMapper<Missile> mMissile;
  protected ComponentMapper<AIWrapper> mAIWrapper;
  protected ComponentMapper<MapWrapper> mMapWrapper;

  protected ObjectInteractor objectInteractor;
  protected WarpInteractor warpInteractor;
  protected ItemInteractor itemInteractor;

  @Override
  public int createPlayer(CharData charData, Vector2 position) {
    int id = super.createEntity(Class.Type.PLR, "player");
    mPlayer.create(id).data = charData;
    mMapWrapper.create(id).set(map, map.getZone(position));

    mPosition.create(id).position.set(position);
    mVelocity.create(id).set(Engine.Player.SPEED_WALK, Engine.Player.SPEED_RUN);
    mAngle.create(id);

    mCofReference.create(id).set(Engine.Player.getToken(charData.getD2S().header.charClass), Class.Type.PLR.DEFAULT_MODE);
    mCofComponents.create(id);
    mCofAlphas.create(id);
    mCofTransforms.create(id);

    mMovementModes.create(id).set(Engine.Player.MODE_TN, Engine.Player.MODE_TW, Engine.Player.MODE_RN);

    mSize.create(id).size = Size.MEDIUM;

    mRunning.create(id);
    mNetworked.create(id);
    mZoneAware.create(id);
    return id;
  }

  @Override
  public int createDynamicObject(int act, int monPresetId, float x, float y) {
    String objectType = Riiablo.files.MonPreset.getPlace(act, monPresetId);
    MonStats.Entry monstats = Riiablo.files.monstats.get(objectType);
    // objectType is a MonStats, MonPlace, SuperUniques
    if (monstats == null) return Engine.INVALID_ENTITY;

    int id = createMonster(monstats.hcIdx, x, y);
    mNetworked.create(id);
    return id;
  }

  @Override
  public int createStaticObject(int act, int objId, float x, float y) {
    int objectType = Riiablo.files.obj.getObjectId(act, objId);
    Objects.Entry base = Riiablo.files.objects.get(objectType);
    if (base == null) return Engine.INVALID_ENTITY;

    int id = super.createEntity(Class.Type.OBJ, base.Description);
    mObject.create(id).base = base;

    mPosition.create(id).position.set(x, y);

    if (base.Draw) {
      mCofReference.create(id).set(base.Token, Class.Type.OBJ.DEFAULT_MODE);
      int[] component = mCofComponents.create(id).component;
      Arrays.fill(component, CofComponents.COMPONENT_NULL);
      mCofAlphas.create(id);
      mCofTransforms.create(id);
    }

    if (base.OperateRange > 0 && ArrayUtils.contains(base.Selectable, true)) {
      mInteractable.create(id).set(base.OperateRange, objectInteractor);
    }

    mSize.create(id); // single size doesn't make any sense in this case because this is a rect
    mNetworked.create(id);
    return id;
  }

  @Override
  public int createMonster(int monsterId, float x, float y) {
    MonStats.Entry monstats = Riiablo.files.monstats.get(monsterId);
    MonStats2.Entry monstats2 = Riiablo.files.monstats2.get(monstats.MonStatsEx);

    int id = super.createEntity(Class.Type.MON, monstats.Id);
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

    mCofAlphas.create(id);
    mCofTransforms.create(id);

    mMovementModes.create(id).set(Engine.Monster.MODE_NU, Engine.Monster.MODE_WL, Engine.Monster.MODE_RN);

    float size = mSize.create(id).size = monstats2.SizeX; // FIXME: SizeX and SizeY appear to always be equal -- is this method sufficient?
    AI ai = mAIWrapper.create(id).findAI(id, monstats.AI).ai;
    world.getInjector().inject(ai);
    ai.initialize();
    if (monstats.interact) {
      mInteractable.create(id).set(size, ai);
    }

    mNetworked.create(id);
    return id;
  }

  @Override
  public int createWarp(int index, float x, float y) {
    final int mainIndex   = DT1.Tile.Index.mainIndex(index);
    final int subIndex    = DT1.Tile.Index.subIndex(index);
    final int orientation = DT1.Tile.Index.orientation(index);

    Map.Zone zone = map.getZone(x, y);
    int dst = zone.level.Vis[mainIndex];
    assert dst > 0 : "Warp to unknown level!";
    int wrp = zone.level.Warp[mainIndex];
    assert wrp >= 0 : "Invalid warp";

    Levels.Entry dstLevel = Riiablo.files.Levels.get(dst);

    LvlWarp.Entry warp = Riiablo.files.LvlWarp.get(wrp);

    int id = super.createEntity(Class.Type.WRP, "warp");
    mWarp.create(id).set(index, warp, dstLevel);
    mMapWrapper.create(id).set(map, zone);

    mPosition.create(id).position.set(x, y).add(warp.OffsetX, warp.OffsetY);

    mInteractable.create(id).set(3.0f, warpInteractor);
    mNetworked.create(id);
    return id;
  }

  @Override
  public int createItem(com.riiablo.item.Item item, Vector2 position) {
    int id = super.createEntity(Class.Type.ITM, "item");
    mItem.create(id).set(item);

    mPosition.create(id).position.set(position);
    mInteractable.create(id).set(1f, itemInteractor);
    return id;
  }

  @Override
  public int createMissile(int missileId, Vector2 angle, Vector2 position) {
    Missiles.Entry missile = Riiablo.files.Missiles.get(missileId);
    int id = super.createEntity(Class.Type.MIS, missile.Missile);
    mMissile.create(id).set(missile, position, missile.Range);

    mPosition.create(id).position.set(position);
    mVelocity.create(id).velocity.set(angle).setLength(missile.Vel);
    mAngle.create(id).set(angle);
    return id;
  }
}
