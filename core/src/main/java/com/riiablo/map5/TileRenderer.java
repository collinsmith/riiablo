package com.riiablo.map5;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

import com.riiablo.codec.util.BBox;
import com.riiablo.util.DebugUtils;

public enum TileRenderer {
  INSTANCE;

  private static final Color GRID_COLOR = new Color().fromHsv(0f, 0f, .15f);

  public void draw(Batch batch, Tile tile, float x, float y) {
    final BBox box = tile.box();
    batch.draw(tile.texture(), x + box.xMin, y + box.yMin);
  }

  public void drawDebug(ShapeRenderer shapes, Tile tile, Block[] blocks, int numBlocks, float x, float y) {
    final boolean initialDrawing = shapes.isDrawing();
    final ShapeRenderer.ShapeType shapeType = shapes.getCurrentType();
    if (!initialDrawing) {
      shapes.begin(ShapeRenderer.ShapeType.Line);
    } else if (shapeType != ShapeRenderer.ShapeType.Line) {
      shapes.end();
      shapes.begin(ShapeRenderer.ShapeType.Line);
    }

    shapes.setColor(Color.GREEN);
    final int xOffs;
    final int yOffs;
    if (tile.orientation == Orientation.FLOOR || tile.orientation == Orientation.ROOF) {
      xOffs = 0;
      yOffs = Tile.HEIGHT;
    } else {
      xOffs = 0;
      yOffs = 0;
    }

    final int PADDING = 2;
    for (int i = 0, s = numBlocks; i < s; i++) {
      final Block block = blocks[i];
      DebugUtils.drawAscii(shapes, Integer.toString(i), x + block.x + xOffs + PADDING, y + -block.y + yOffs - PADDING, Align.topLeft);
    }

    if (!initialDrawing) {
      shapes.end();
    } else if (shapeType != shapes.getCurrentType()) {
      shapes.end();
      shapes.begin(shapeType);
    }
  }

  public void drawDebug(ShapeRenderer shapes, Tile tile, float x, float y) {
    final boolean initialDrawing = shapes.isDrawing();
    final ShapeRenderer.ShapeType shapeType = shapes.getCurrentType();
    if (!initialDrawing) {
      shapes.begin(ShapeRenderer.ShapeType.Line);
    } else if (shapeType != ShapeRenderer.ShapeType.Line) {
      shapes.end();
      shapes.begin(ShapeRenderer.ShapeType.Line);
    }

    final BBox box = tile.box();
    shapes.setColor(GRID_COLOR);
    for (int yOffs = box.yMin; yOffs < box.yMax; yOffs += Block.RLE_HEIGHT) {
      shapes.line(x + box.xMin, y + yOffs, x + box.xMax, y + yOffs);
    }
    for (int xOffs = box.xMin; xOffs < box.xMax; xOffs += Block.RLE_WIDTH) {
      shapes.line(x + xOffs, y + box.yMin, x + xOffs, y + box.yMax);
    }

    shapes.setColor(Color.WHITE);
    shapes.line(x + Tile.WIDTH50, y + Tile.HEIGHT, x + Tile.WIDTH50, y + Tile.HEIGHT + Tile.WALL_HEIGHT);
    DebugUtils.drawDiamond2(shapes, x, y, Tile.WIDTH, Tile.HEIGHT);

    shapes.setColor(Color.GREEN); // self-reported tile size +1px padding
    shapes.rect(x - 1, y + box.yMin - 1, tile.width + 2, -tile.height + 2);

    shapes.setColor(Color.RED); // texture -1px padding
    shapes.rect(x + box.xMin + 1, y + box.yMin + 1, tile.texture().getRegionWidth() - 2, tile.texture().getRegionHeight() - 2);

    shapes.setColor(Color.WHITE); // bbox
    shapes.rect(x + box.xMin, y + box.yMin, box.width, box.height);

    shapes.setColor(Color.MAGENTA);
    shapes.line(x - Tile.WIDTH, y, x + Tile.WIDTH + Tile.WIDTH, y);
    shapes.end();
    shapes.begin(ShapeRenderer.ShapeType.Filled);
    shapes.rect(x - 3, y - 3, 6, 6);

    if (!initialDrawing) {
      shapes.end();
    } else if (shapeType != shapes.getCurrentType()) {
      shapes.end();
      shapes.begin(shapeType);
    }
  }

  private static final int[][] SUBTILE_OFFSET/* = {
      {64, 64}, {80, 56}, {96, 48}, {112, 40}, {128, 32},
      {48, 56}, {64, 48}, {80, 40}, { 96, 32}, {112, 24},
      {32, 48}, {48, 40}, {64, 32}, { 80, 24}, { 96, 16},
      {16, 40}, {32, 32}, {48, 24}, { 64, 16}, { 80,  8},
      { 0, 32}, {16, 24}, {32, 16}, { 48,  8}, { 64,  0},
  }*/;
  static {
    SUBTILE_OFFSET = new int[Tile.NUM_SUBTILES][2];
    for (int y = 0, i = 0; y < Tile.SUBTILE_SIZE; y++) {
      int px = (Tile.WIDTH / 2) - Tile.SUBTILE_WIDTH50 - (y * Tile.SUBTILE_WIDTH50);
      int py = Tile.HEIGHT      - Tile.SUBTILE_HEIGHT  - (y * Tile.SUBTILE_HEIGHT50);
      for (int x = 0; x < Tile.SUBTILE_SIZE; x++, i++) {
        SUBTILE_OFFSET[i][0] = px;
        SUBTILE_OFFSET[i][1] = py;
        px += Tile.SUBTILE_WIDTH50;
        py -= Tile.SUBTILE_HEIGHT50;
      }
    }
  }

  public void drawDebugFlags(ShapeRenderer shapes, Tile tile, float x, float y) {
    for (int t = 0; t < Tile.NUM_SUBTILES; t++) {
      int flags = tile.flags[t] & 0xFF;
      if (flags == 0) continue;
      float offX = x + SUBTILE_OFFSET[t][0];
      float offY = y + SUBTILE_OFFSET[t][1];
      drawDebugFlags(shapes, flags, offX, offY);
    }
  }

  public void drawDebugFlags(ShapeRenderer shapes, int flags, float offX, float offY) {
    shapes.setColor(Color.CORAL);
    shapes.set(ShapeRenderer.ShapeType.Line);
    DebugUtils.drawDiamond2(shapes, offX, offY, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
    shapes.set(ShapeRenderer.ShapeType.Filled);

    offY += Tile.SUBTILE_HEIGHT50;

    if ((flags & Tile.FLAG_BLOCK_WALK) != 0) {
      shapes.setColor(Color.FIREBRICK);
      shapes.triangle(
          offX + 16, offY,
          offX + 16, offY + 8,
          offX + 24, offY + 4);
    }
    if ((flags & Tile.FLAG_BLOCK_LIGHT_LOS) != 0) {
      shapes.setColor(Color.FOREST);
      shapes.triangle(
          offX + 16, offY,
          offX + 32, offY,
          offX + 24, offY + 4);
    }
    if ((flags & Tile.FLAG_BLOCK_JUMP) != 0) {
      shapes.setColor(Color.ROYAL);
      shapes.triangle(
          offX + 16, offY,
          offX + 32, offY,
          offX + 24, offY - 4);
    }
    if ((flags & Tile.FLAG_BLOCK_PLAYER_WALK) != 0) {
      shapes.setColor(Color.VIOLET);
      shapes.triangle(
          offX + 16, offY,
          offX + 16, offY - 8,
          offX + 24, offY - 4);
    }
    if ((flags & Tile.FLAG_BLOCK_UNKNOWN1) != 0) {
      shapes.setColor(Color.GOLD);
      shapes.triangle(
          offX + 16, offY,
          offX + 16, offY - 8,
          offX + 8, offY - 4);
    }
    if ((flags & Tile.FLAG_BLOCK_LIGHT) != 0) {
      shapes.setColor(Color.SKY);
      shapes.triangle(
          offX, offY,
          offX + 16, offY,
          offX + 8, offY - 4);
    }
    if ((flags & Tile.FLAG_BLOCK_UNKNOWN2) != 0) {
      shapes.setColor(Color.WHITE);
      shapes.triangle(
          offX, offY,
          offX + 16, offY,
          offX + 8, offY + 4);
    }
    if ((flags & Tile.FLAG_BLOCK_UNKNOWN3) != 0) {
      shapes.setColor(Color.SLATE);
      shapes.triangle(
          offX + 16, offY,
          offX + 16, offY + 8,
          offX + 8, offY + 4);
    }
  }
}
