package com.riiablo.engine.server;

import com.artemis.ComponentMapper;
import com.artemis.annotations.Wire;
import net.mostlyoriginal.api.system.core.PassiveSystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import com.riiablo.codec.excel.LvlWarp;
import com.riiablo.engine.Engine;
import com.riiablo.engine.server.component.Box2DBody;
import com.riiablo.engine.server.component.Interactable;
import com.riiablo.engine.server.component.MapWrapper;
import com.riiablo.engine.server.component.Position;
import com.riiablo.engine.server.component.Warp;
import com.riiablo.map.Map;

public class WarpInteractor extends PassiveSystem implements Interactable.Interactor {
  private static final String TAG = "WarpInteractor";

  protected ComponentMapper<Warp> mWarp;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<MapWrapper> mMapWrapper;
  protected ComponentMapper<Box2DBody> mBox2DBody;

  protected Pathfinder pathfinder;
  protected Actioneer actioneer;

  @Wire(name = "map")
  protected Map map;

  private final Vector2 tmpVec2 = new Vector2();

  @Override
  public void interact(int src, int entity) {
    Gdx.app.log(TAG, "zim zim zala bim");
    Warp warp = mWarp.get(entity);
    Map.Zone dst = map.findZone(warp.dstLevel);
    int dstIndex = mMapWrapper.get(entity).zone.getWarp(warp.index);
    int dstWarpEntity = dst.findWarp(dstIndex);
    if (dstWarpEntity == Engine.INVALID_ENTITY) throw new AssertionError("Invalid dstWarp: " + dstIndex);
    Vector2 dstWarpPos = mPosition.get(dstWarpEntity).position;
    Vector2 position = mPosition.get(src).position;
    position.set(dstWarpPos);

    Box2DBody box2dWrapper = mBox2DBody.get(src);
    if (box2dWrapper != null) box2dWrapper.body.setTransform(position, 0);

    Warp dstWarp = mWarp.get(dstWarpEntity);
    LvlWarp.Entry dstWarpEntry = dstWarp.warp;
    tmpVec2.set(dstWarpPos).add(dstWarpEntry.ExitWalkX, dstWarpEntry.ExitWalkY);
    actioneer.moveTo(src, tmpVec2);
  }
}
