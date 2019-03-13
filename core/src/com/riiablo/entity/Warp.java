package com.riiablo.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.Levels;
import com.riiablo.codec.excel.LvlWarp;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.Map;

import static com.riiablo.map.DT1.Tile;

public class Warp extends Entity {

  public final Map      map;
  public final Map.Zone zone;
  public final int      index;

  public final LvlWarp.Entry warp;
  public final Levels.Entry  dstLevel;

  public final Vector2 pixelLoc;

  BBox box;

  public Warp(Map map, Map.Zone zone, int index, int x, int y) {
    super(Type.WRP, "warp", null);
    this.map   = map;
    this.zone  = zone;
    this.index = index;

    int dst = zone.level.Vis[index];
    assert dst > 0 : "Warp to unknown level!";
    int wrp = zone.level.Warp[index];
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

    /**
     * TODO: warp.LitVersion determines whether or not warp has special highlighted tiles
     *       warp.Tile is added to subindex of tile to find corresponding highlighted tiles (if warp.LitVersion set)
     */
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
    label.setPosition(pixelLoc.x, pixelLoc.y + getLabelOffset() + label.getHeight() / 2, Align.center);
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
