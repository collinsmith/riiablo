package com.riiablo.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DT1;
import com.riiablo.map.Map;

import static com.riiablo.map.DT1.Tile;

public class Warp extends Entity {
  private static final IntIntMap EMPTY_INT_INT_MAP = new IntIntMap();

  public final Map      map;
  public final Map.Zone zone;

  public final LvlWarp.Entry warp;
  public final Levels.Entry  dstLevel;

  public final Vector2 pixelLoc;

  BBox box;
  public final IntIntMap substs;

  public Warp(Map map, Map.Zone zone, int orientation, int mainIndex, int subIndex, int x, int y) {
    super(Type.WRP, "warp", null);
    this.map  = map;
    this.zone = zone;

    int dst = zone.level.Vis[mainIndex];
    assert dst > 0 : "Warp to unknown level!";
    int wrp = zone.level.Warp[mainIndex];
    assert wrp >= 0 : "Invalid warp";

    dstLevel = Riiablo.files.Levels.get(dst);
    name(dstLevel.LevelWarp);

    warp = Riiablo.files.LvlWarp.get(wrp);
    position.set(x, y).add(warp.OffsetX, warp.OffsetY);

    box = new BBox();
    box.xMin = warp.SelectX;
    box.yMin = warp.SelectY;
    box.width = warp.SelectDX;
    box.height = warp.SelectDY;
    box.xMax = box.width + box.xMin;
    box.yMax = box.height + box.yMin;

    pixelLoc = new Vector2();
    pixelLoc.x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    pixelLoc.y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);

    if (warp.LitVersion) {
      substs = new IntIntMap();
      if (subIndex < 2) {
        substs.put(DT1.Tile.Index.create(orientation, mainIndex, 0), DT1.Tile.Index.create(orientation, mainIndex, 2));
        substs.put(DT1.Tile.Index.create(orientation, mainIndex, 1), DT1.Tile.Index.create(orientation, mainIndex, 3));
      } else {
        substs.put(DT1.Tile.Index.create(0, subIndex, 0), DT1.Tile.Index.create(0, subIndex, 4));
        substs.put(DT1.Tile.Index.create(0, subIndex, 1), DT1.Tile.Index.create(0, subIndex, 5));
        substs.put(DT1.Tile.Index.create(0, subIndex, 2), DT1.Tile.Index.create(0, subIndex, 6));
        substs.put(DT1.Tile.Index.create(0, subIndex, 3), DT1.Tile.Index.create(0, subIndex, 7));
      }
    } else {
      substs = EMPTY_INT_INT_MAP;
    }
  }

  @Override
  public void setOver(boolean b) {
    super.setOver(b);
    if (substs != EMPTY_INT_INT_MAP) {
      if (b) {
        map.addWarpSubsts(substs);
      } else {
        map.clearWarpSubsts(substs);
      }
    }
  }

  @Override
  public boolean isSelectable() {
    return true;
  }

  @Override
  public float getLabelOffset() {
    return 0;
  }

  @Override
  public boolean contains(Vector2 coords) {
    float x = pixelLoc.x + box.xMin;
    float y = pixelLoc.y - box.yMax;
    return x <= coords.x && coords.x <= x + box.width
       &&  y <= coords.y && coords.y <= y + box.height;
  }

  @Override
  protected void updateCOF() {

  }

  @Override
  public void draw(PaletteIndexedBatch batch) {
    label.setPosition(pixelLoc.x + box.xMin + box.width / 2, pixelLoc.y - box.yMax + box.height / 2 + getLabelOffset() + label.getHeight() / 2, Align.center);
  }

  @Override
  public void drawShadow(PaletteIndexedBatch batch) {

  }

  @Override
  public void drawDebugStatus(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    super.drawDebugStatus(batch, shapes);
    shapes.setColor(Color.GREEN);
    shapes.rect(pixelLoc.x + box.xMin, pixelLoc.y - box.yMax, box.width, box.height);
  }
}
