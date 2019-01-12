package gdx.diablo.codec;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Comparator;

public class Map implements Disposable {
  private static final String TAG = "Map";
  private static final boolean DEBUG      = true;
  private static final boolean DEBUG_DT1S = DEBUG && true;
  private static final boolean DEBUG_BT   = DEBUG && false;

  DS1 ds1;
  DT1 dt1s[];

  BlockTable blockTable;

  public Map(DS1 ds1, DT1[] dt1s) {
    this.ds1 = ds1;
    this.dt1s = dt1s;
    if (DEBUG_DT1S) {
      StringBuilder builder = new StringBuilder();
      builder.append('\n');
      builder
          .append("dt1 file").append('\n')
          .append("--- ---------------------------------------------------------------------").append('\n');

      for (int i = 0; i < dt1s.length; i++) {
        builder.append(String.format("%3d %s", i, dt1s[i].fileName)).append('\n');
      }

      Gdx.app.debug(TAG, builder.toString());
    }

    blockTable = new BlockTable(dt1s);
    mapPropToBlocks();
  }

  public DS1 getDS1() {
    return ds1;
  }

  public int getWidth() {
    return ds1.getWidth();
  }

  public int getHeight() {
    return ds1.getHeight();
  }

  private void mapPropToBlocks() {
    int wall = 0, floor = 0, shadow = 0;
    for (int y = 0; y < ds1.height; y++) {
      for (int x = 0; x < ds1.width; x++) {
        for (int i = 0; i < ds1.numWalls; i++) {
          mapWallPropToBlock(ds1.walls[wall++]);
        }

        for (int i = 0; i < ds1.numFloors; i++) {
          mapFloorPropToBlock(ds1.floors[floor++]);
        }

        for (int i = 0; i < ds1.numShadows; i++) {
          mapShadowPropToBlock(ds1.shadows[shadow++]);
        }
      }
    }
  }

  private void mapWallPropToBlock(DS1.Wall wall) {
    wall.blockIndex = -1;
    if (wall.prop1 == 0) {
      wall.blockIndex = -1;
      return;
    }

    int orientation = wall.orientation;
    int mainIndex = (wall.prop3 >> 4) + ((wall.prop4 & 0x03) << 4);
    int subIndex  = wall.prop2;
    for (int b = 0; b < blockTable.numBlocks; b++) {
      BlockTable.Block block = blockTable.blocks[b];
      if (block.tile.orientation == orientation
       && block.tile.mainIndex == mainIndex
       && block.tile.subIndex == subIndex
       && block.used_by_game) {
        wall.blockIndex = b;
        return;
      }
    }

    // trick of O=18 M=3 I=1 ---> O=19 M=3 I=0
    if ((orientation == 18) || (orientation == 19)) {
      if (orientation == 18)
        orientation = 19;
      else
        orientation = 18;

      // search with same sub_index (just in case)
      for (int b = 0; b < blockTable.numBlocks; b++) {
        BlockTable.Block block = blockTable.blocks[b];
        if (block.tile.orientation == orientation
         && block.tile.mainIndex == mainIndex
         && block.tile.subIndex == subIndex
         && block.used_by_game) {
          wall.blockIndex = b;
          return;
        }
      }

      // search with sub_index = 0
      subIndex = 0;
      for (int b = 0; b < blockTable.numBlocks; b++) {
        BlockTable.Block block = blockTable.blocks[b];
        if (block.tile.orientation == orientation
         && block.tile.mainIndex == mainIndex
         && block.tile.subIndex == subIndex
         && block.used_by_game) {
          wall.blockIndex = b;
          return;
        }
      }
    }
  }

  private void mapFloorPropToBlock(DS1.Floor floor) {
    floor.blockIndex = -1;
    if (floor.prop1 == 0) {
      floor.blockIndex = -1;
      return;
    }

    int mainIndex = (floor.prop3 >> 4) + ((floor.prop4 & 0x03) << 4);
    int subIndex  = floor.prop2;
    for (int b = 0; b < blockTable.numBlocks; b++) {
      BlockTable.Block block = blockTable.blocks[b];
      if (block.tile.orientation == 0
       && block.tile.mainIndex == mainIndex
       && block.tile.subIndex == subIndex
       && block.used_by_game) {
        floor.blockIndex = b;
        return;
      }
    }
  }

  private void mapShadowPropToBlock(DS1.Shadow shadow) {
    shadow.blockIndex = -1;
    if (shadow.prop1 == 0) {
      shadow.blockIndex = -1;
      return;
    }

    int mainIndex = (shadow.prop3 >> 4) + ((shadow.prop4 & 0x03) << 4);
    int subIndex  = shadow.prop2;
    for (int b = 0; b < blockTable.numBlocks; b++) {
      BlockTable.Block block = blockTable.blocks[b];
      if (block.tile.orientation == 13
       && block.tile.mainIndex == mainIndex
       && block.tile.subIndex == subIndex
       && block.used_by_game) {
        shadow.blockIndex = b;
        return;
      }
    }
  }

  /**
   * @param pOffX  player x offset of (tx, ty) center
   * @param pOffY  player y offset of (tx, ty) center
   * @param tx     tile x (between 0 .. ds1.width)
   * @param ty     tile y (between 0 .. ds1.height)
   * @param width  width of screen
   * @param height height of screen
   * @param factor distance to render screen outside of (1.5 is safe)
   */
  public void draw(Batch b, int pOffX, int pOffY, int tx, int ty, int width, int height, float factor) {
    final int tileWidth = ds1.tileWidth;
    final int tileHeight = ds1.tileHeight;
    final int halfTileWidth = tileWidth >>> 1;
    final int halfTileHeight = tileHeight >>> 1;

    final int cx = width >>> 1;
    final int cy = height >>> 1;
    //System.out.println("cx = " + cx + ", cy = " + cy);

    width *= factor;
    height *= factor;

    final int tilesX = (width + tileWidth - 1) / tileWidth;
    final int tilesY = (height + tileHeight - 1) / tileHeight;
    //System.out.println("tx = " + tx + ", ty = " + ty);
    //System.out.println("tilesX = " + tilesX + ", tilesY = " + tilesY);

    int offX = tilesX / 2;
    int offY = tilesY / 2;
    //System.out.println("offX = " + offX + ", offY = " + offY);

    final int startX = tx + offX - offY;
    final int startY = ty - offX - offY;
    //System.out.println("startX = " + startX + ", startY = " + startY);

    int[] buffer = new int[tilesX + tilesY - 1];
    final int bufferMid = buffer.length / 2;
    for (int x = 0; x < bufferMid; x++) {
      buffer[x] = 1 + x * 2;
    }

    for (int x = buffer.length - 1; x >= bufferMid; x--) {
      buffer[x] = 1 + (buffer.length - x - 1) * 2;
    }

    //System.out.println(Arrays.toString(buffer));

    final int startPx, startPy;
    if ((tilesX & 1) == 1) {
      startPx = cx + (tilesX * halfTileWidth) - tileWidth;
    } else {
      startPx = cx + (tilesX * halfTileWidth) - halfTileWidth;
    }
    if ((tilesY & 1) == 1) {
      startPy = cy + (tilesY * halfTileHeight) - tileHeight;
    } else {
      startPy = cy + (tilesY * halfTileHeight) - halfTileHeight;
    }
    //System.out.println("startPx = " + startPx + ", startPy = " + startPy);

    int size;
    int startX2, startY2, dx, dy;
    int startPx2, startPy2, px, py;

    startX2 = startX;
    startY2 = startY;
    startPx2 = startPx;
    startPy2 = startPy;
    for (int y = 0; y < buffer.length; y++) {
      dx = startX2;
      dy = startY2;
      px = startPx2;
      py = startPy2;
      size = buffer[y];
      for (int x = 0; x < size; x++) {
        if (0 <= dy && dy < ds1.height
         && 0 <= dx && dx < ds1.width) {
          //System.out.println(dx + ", " + dy + "; " + px + ", " + py);
          drawWalls(b, dx, dy, px, py, false);
          drawFloors(b, dx, dy, px, py);
          drawShadows(b, dx, dy, px, py);
        }
        dx++;
        px += halfTileWidth;
        py -= halfTileHeight;
      }

      startY2++;
      if (y >= bufferMid) {
        startX2++;
        startPy2 -= tileHeight;
      } else {
        startX2--;
        startPx2 -= tileWidth;
      }
    }

    startX2 = startX;
    startY2 = startY;
    startPx2 = startPx;
    startPy2 = startPy;
    for (int y = 0; y < buffer.length; y++) {
      dx = startX2;
      dy = startY2;
      px = startPx2;
      py = startPy2;
      size = buffer[y];
      for (int x = 0; x < size; x++) {
        if (0 <= dy && dy < ds1.height
         && 0 <= dx && dx < ds1.width) {
          drawWalls(b, dx, dy, px, py, true);
        }
        dx++;
        px += halfTileWidth;
        py -= halfTileHeight;
      }

      startY2++;
      if (y >= bufferMid) {
        startX2++;
        startPy2 -= tileHeight;
      } else {
        startX2--;
        startPx2 -= tileWidth;
      }
    }

    startX2 = startX;
    startY2 = startY;
    startPx2 = startPx;
    startPy2 = startPy;
    for (int y = 0; y < buffer.length; y++) {
      dx = startX2;
      dy = startY2;
      px = startPx2;
      py = startPy2;
      size = buffer[y];
      for (int x = 0; x < size; x++) {
        if (0 <= dy && dy < ds1.height
         && 0 <= dx && dx < ds1.width) {
          drawRoofs(b, dx, dy, px, py);
        }
        dx++;
        px += halfTileWidth;
        py -= halfTileHeight;
      }

      startY2++;
      if (y >= bufferMid) {
        startX2++;
        startPy2 -= tileHeight;
      } else {
        startX2--;
        startPx2 -= tileWidth;
      }
    }
  }

  public void drawDebug(ShapeRenderer shapes) {
    int px = 0;
    int py = 0;

    shapes.setColor(Color.WHITE);
    for (int x = 0; x < ds1.width; x++) {
      for (int y = 0; y < ds1.height; y++) {
        shapes.line(px, py, px + 160, py - 40);
      }
    }
  }

  public void draw(Batch b, int dx, int dy) {
    //int cx = ds1.width / 2 + 1;
    //int cy = ds1.height / 2;

    int width = Gdx.graphics.getWidth();
    int height = Gdx.graphics.getHeight();

    final int tileWidthShift = ds1.tileWidth / 2;
    final int tileHeightShift = ds1.tileHeight / 2;

    //int px = 0;
    //int py = 0;
    final int px = width / 2 - tileWidthShift - 1000;
    final int py = height - tileHeightShift;

    int tx, ty;
    // loop 1A : lower walls, floors, shadows of dt1
    for (int x = 0; x < ds1.width; x++) {
      tx = px + x * tileWidthShift;
      ty = py - x * tileHeightShift;
      //if (tx < 0 || ty < 0 || tx > width || ty > height) {
      //  continue;
      //}

      for (int y = 0; y < ds1.height; y++) {
        drawWalls(b, x, y, tx, ty, false);
        drawFloors(b, x, y, tx, ty);
        drawShadows(b, x, y, tx, ty);
        tx -= tileWidthShift;
        ty -= tileHeightShift;
        //if (tx < 0 || ty < 0 || tx > width || ty > height) {
        //  break;
        //}
      }
    }

    // loop 1B : shadows of objects
    //for (;;) {}

    // loop 2 : objects with orderflag set to 1 (optional)

    // tile grid : if over floor but under wall, draw it now


    // loop 3 : upper walls, objects with orderflag set to 0 or 2
    for (int x = 0; x < ds1.width; x++) {
      tx = px + x * tileWidthShift;
      ty = py - x * tileHeightShift;
      for (int y = 0; y < ds1.height; y++) {
        drawWalls(b, x, y, tx, ty, true);
        tx -= tileWidthShift;
        ty -= tileHeightShift;
      }
    }

    // loop 4 : roofs
    for (int x = 0; x < ds1.width; x++) {
      tx = px + x * tileWidthShift;
      ty = py - x * tileHeightShift;
      for (int y = 0; y < ds1.height; y++) {
        drawRoofs(b, x, y, tx, ty);
        tx -= tileWidthShift;
        ty -= tileHeightShift;
      }
    }

    // loop 5 : special tiles (optional)

    // loop 6 : walkable infos (optional)

    // tile grid : if over floor and walls, draw it now

    // npc paths

    // objects infos
  }

  public void drawFloors(Batch b, int x, int y, int mx, int my) {
    int f_ptr = (y * ds1.floorLine) + (x * ds1.numFloors);
    OrderData[] orderData = new OrderData[ds1.numFloors];
    for (int i = 0; i < ds1.numFloors; i++) {
      orderData[i] = new OrderData(f_ptr + i, ds1.floors[f_ptr + i].prop1);
    }

    Arrays.sort(orderData, new Comparator<OrderData>() {
      @Override
      public int compare(OrderData d1, OrderData d2) {
        if (d1.height == d2.height) {
          return d1.id - d2.id;
        }

        return d1.height - d2.height;
      }
    });

    for (int i = 0; i < ds1.numFloors; i++) {
      DS1.Floor floor = ds1.floors[orderData[i].id];

      int bt_idx = floor.blockIndex;
      //if (bt_idx == 0) {
        //System.out.println("stop bt_idx == 0");
      //  continue;
      //}

      if ((floor.prop4 & 0x80) != 0) {
        bt_idx = -1;
      }

      if (bt_idx < 0) {
        //System.out.println("stop bt_idx <= 0");
        continue;
      }

      BlockTable.Block block = blockTable.blocks[bt_idx];
      if (block.type != BlockTable.Block.BlockType.STATIC && block.type != BlockTable.Block.BlockType.ANIMATED) {
        //System.out.println("stop block.type = " + block.type);
        continue;
      }

      int dt1Index   = block.dt1Index;
      int blockIndex = block.blockIndex;
      b.draw(block.texture, mx, my, block.texture.getRegionWidth() + 1, block.texture.getRegionHeight() + 1);
      //System.out.println("drawing " + block);
    }
  }

  public void drawWalls(Batch b, int x, int y, int mx, int my, boolean upper) {
    int w_ptr = (y * ds1.wallLine) + (x * ds1.numWalls);
    OrderData[] orderData = new OrderData[ds1.numWalls];
    for (int i = 0; i < ds1.numWalls; i++) {
      orderData[i] = new OrderData(w_ptr + i, ds1.walls[w_ptr + i].prop1);
      if (ds1.walls[w_ptr + i].orientation == 10) {
        orderData[i].height = 255;
      }
    }

    Arrays.sort(orderData, new Comparator<OrderData>() {
      @Override
      public int compare(OrderData d1, OrderData d2) {
        if (d1.height == d2.height) {
          return d1.id - d2.id;
        }

        return d1.height - d2.height;
      }
    });

    for (int i = 0; i < ds1.numWalls; i++) {
      DS1.Wall wall = ds1.walls[orderData[i].id];
      int orientation = wall.orientation;
      if (upper && orientation >= 15) continue;
      if (!upper && orientation <= 15) continue;

      int bt_idx = wall.blockIndex;
      if (bt_idx < 0) {
        continue;
      }

      // this hides special tiles, e.g., "Town Portal" tile
      if (orientation == 10 || orientation == 11) {
        bt_idx = -1;
      }

      if ((wall.prop4 & 0x80) != 0) {
        if (orientation != 10 && orientation != 11) {
          bt_idx = -1;
        }
      }

      if (bt_idx < 0) {
        continue;
      }

      BlockTable.Block block = blockTable.blocks[bt_idx];
      if (block.type != BlockTable.Block.BlockType.WALL_UP
       && block.type != BlockTable.Block.BlockType.WALL_DOWN
       && block.type != BlockTable.Block.BlockType.SPECIAL) {
        continue;
      }

      // FIXME: lower walls need an offset applied here
      //if (!upper) {
      //  my += block.zeroLine;
      //}
      my += block.zeroLine;
      b.draw(block.texture, mx, my);
      if (orientation == 3) {
        int m = block.tile.mainIndex;
        int s = block.tile.subIndex;
        boolean found = false;
        int tempId = bt_idx;
        for (;;) {
          if (bt_idx >= blockTable.numBlocks) {
            break;
          } else {
            BlockTable.Block otherBlock = blockTable.blocks[++tempId];
            if (otherBlock.tile.orientation < 4) {
              bt_idx++;
            } else {
              if (otherBlock.tile.orientation == 4) {
                if (otherBlock.tile.mainIndex == m && otherBlock.tile.subIndex == s) {
                  found = true;
                  break;
                }
              }
            }
          }
        }

        if (found) {
          b.draw(blockTable.blocks[tempId].texture, mx, my);
        }
      }
    }
  }

  public void drawShadows(Batch b, int x, int y, int mx, int my) {
    int s_ptr = (y * ds1.shadowLine) + (x * ds1.numShadows);
    for (int i = 0; i < ds1.numShadows; i++) {
      DS1.Shadow shadow = ds1.shadows[s_ptr + i];
      int bt_idx = shadow.blockIndex;
      if ((shadow.prop4 & 0x80) != 0) {
        bt_idx = -1;
      }

      if (bt_idx < 0) {
        continue;
      }

      BlockTable.Block block = blockTable.blocks[bt_idx];
      // TODO: layer mask render /w transparencies
      b.draw(block.texture, mx, my);
    }
  }

  public void drawRoofs(Batch b, int x, int y, int mx, int my) {
    int f_ptr = (y * ds1.wallLine) + (x * ds1.numWalls);
    OrderData[] orderData = new OrderData[ds1.numWalls];
    for (int i = 0; i < ds1.numWalls; i++) {
      orderData[i] = new OrderData(f_ptr + i, ds1.walls[f_ptr + i].prop1);
    }

    Arrays.sort(orderData, new Comparator<OrderData>() {
      @Override
      public int compare(OrderData d1, OrderData d2) {
        if (d1.height == d2.height) {
          return d1.id - d2.id;
        }

        return d1.height - d2.height;
      }
    });

    for (int i = 0; i < ds1.numWalls; i++) {
      DS1.Wall wall = ds1.walls[orderData[i].id];
      int bt_idx = wall.blockIndex;
      if (bt_idx < 0) {
        continue;
      }

      if ((wall.prop4 & 0x80) != 0) {
        bt_idx = -1;
      }

      if (bt_idx < 0) {
        continue;
      }

      BlockTable.Block block = blockTable.blocks[bt_idx];
      if (block.type != BlockTable.Block.BlockType.ROOF) {
        continue;
      }

      b.draw(block.texture, mx, my + block.tile.roofHeight);
    }
  }

  @Override
  public void dispose() {
    for (BlockTable.Block block : blockTable.blocks) {
      block.texture.getTexture().dispose();
    }
  }

  static class OrderData {
    int id;
    int height;

    OrderData(int id, int height) {
      this.id = id;
      this.height = height;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("id", id)
          .append("height", height)
          .build();
    }
  }

  static class BlockTable {
    int   numBlocks;
    Block blocks[];

    BlockTable(DT1[] dt1s) {
      numBlocks = 0;
      for (DT1 dt1 : dt1s) {
        numBlocks += dt1.header.numTiles;
      }

      blocks = new Block[numBlocks];
      for (int dt1Index = 0, b = 0; dt1Index < dt1s.length; dt1Index++) {
        DT1 dt1 = dt1s[dt1Index];
        DT1.Tile[] tiles = dt1.tiles;
        for (int blockIndex = 0; blockIndex < tiles.length; blockIndex++) {
          DT1.Tile tile = tiles[blockIndex];
          blocks[b++] = new Block(dt1, dt1Index, blockIndex, tile);
        }
      }

      Arrays.sort(blocks, new Comparator<Block>() {
        @Override
        public int compare(Block b1, Block b2) {
          int n1 = 0;
          int n2 = 0;
          if (b1.tile.orientation != b2.tile.orientation) {
            n1 = b1.tile.orientation;
            n2 = b2.tile.orientation;
          } else if (b1.tile.mainIndex != b2.tile.mainIndex) {
            n1 = b1.tile.mainIndex;
            n2 = b2.tile.mainIndex;
          } else if (b1.tile.subIndex != b2.tile.subIndex) {
            n1 = b1.tile.subIndex;
            n2 = b2.tile.subIndex;
          } else if (b1.dt1Index != b2.dt1Index) {
            n1 = b1.dt1Index;
            n2 = b2.dt1Index;
          } else if (b1.rarity != b2.rarity) {
            n1 = b1.rarity;
            n2 = b2.rarity;
          } else if (b1.blockIndex != b2.blockIndex) {
            n1 = b1.blockIndex;
            n2 = b2.blockIndex;
          }

          return n1 - n2;
        }
      });

      // TODO: It seems the game might combine these into a single frame see
      //       data\global\tiles\expansion\Town\townWest.ds1 tile (8, 18)
      //       index 25 and 83 in data\global\tiles\expansion\Town\buildingses.dt1
      //       could be some of those unknown values assist with this problem? Maybe they prioritise
      misc_check_tiles_conflicts();

      Arrays.sort(blocks, new Comparator<Block>() {
        @Override
        public int compare(Block b1, Block b2) {
          int n1 = 0;
          int n2 = 0;
          if (b1.tile.mainIndex != b2.tile.mainIndex) {
            n1 = b1.tile.mainIndex;
            n2 = b2.tile.mainIndex;
          } else if (b1.tile.orientation != b2.tile.orientation) {
            n1 = b1.tile.orientation;
            n2 = b2.tile.orientation;
          } else if (b1.tile.subIndex != b2.tile.subIndex) {
            n1 = b1.tile.subIndex;
            n2 = b2.tile.subIndex;
          } else if (b1.dt1Index != b2.dt1Index) {
            n1 = b1.dt1Index;
            n2 = b2.dt1Index;
          } else if (b1.rarity != b2.rarity) {
            n1 = b1.rarity;
            n2 = b2.rarity;
          } else if (b1.blockIndex != b2.blockIndex) {
            n1 = b1.blockIndex;
            n2 = b2.blockIndex;
          }

          return n1 - n2;
        }
      });

      if (DEBUG_BT) {
        StringBuilder builder = new StringBuilder();
        builder.append("sorted block_table of ").append(numBlocks).append(" blocks:").append('\n');
        builder
            .append("block orientation mainIndex subIndex rarity dt1_idx blk_idx roofHeight type zeroLine conflict").append('\n')
            .append("----- ----------- --------- -------- ------ ------- ------- ---------- ---- -------- --------").append('\n');
        for (int i = 0; i < numBlocks; i++) {
          Block block = blocks[i];
          builder.append(String.format("%5d %11d %9d %8d %6d %7d %7d %10d %4d %8d",
              i, block.tile.orientation, block.tile.mainIndex, block.tile.subIndex, block.rarity,
              block.dt1Index + 1, block.blockIndex, // FIXME: index is offset to help with debugging, undo later
              block.tile.roofHeight, block.type.ordinal(), block.zeroLine));
          block.conflict = false;
          if (block.rarity == 0) {
            if (i > 1) {
              Block b2 = blocks[i - 1];
              if (block.tile.orientation == b2.tile.orientation
               && block.tile.mainIndex   == b2.tile.mainIndex
               && block.tile.subIndex    == b2.tile.subIndex) {
                block.conflict = true;
              }
            }

            if (i < numBlocks - 1) {
              Block b2 = blocks[i + 1];
              if (block.tile.orientation == b2.tile.orientation
               && block.tile.mainIndex   == b2.tile.mainIndex
               && block.tile.subIndex    == b2.tile.subIndex) {
                block.conflict = true;
              }
            }
          }

          if (block.conflict) {
            builder.append(" *");
          }

          builder.append('\n');
        }

        Gdx.app.debug(TAG, builder.toString());
      }
    }

    void misc_check_tiles_conflicts() {
      int i;
      int start_i = 0, end_i;
      int old_o, old_m, old_s, old_d, o, m, s, d, r;
      int max_rarity, sum_rarity;
      for (;;) {
        Block block = blocks[start_i];
        old_o = block.tile.orientation;
        old_m = block.tile.mainIndex;
        old_s = block.tile.subIndex;
        old_d = block.dt1Index;

        block.used_by_game = false;

        Block lastBlock = block;
        sum_rarity = block.rarity;
        max_rarity = Integer.MIN_VALUE;

        Block firstBlock;
        if (block.rarity > 0) {
          firstBlock = block;
        } else {
          firstBlock = null;
        }

        for (i = start_i + 1; i < numBlocks; i++) {
          Block next = blocks[i];
          o = next.tile.orientation;
          m = next.tile.mainIndex;
          s = next.tile.subIndex;
          d = next.dt1Index;
          r = next.rarity;
          if (old_o != o || old_m != m || old_s != s) {
            break;
          }

          next.used_by_game = false;
          if (d == old_d) {
            lastBlock = next;
          }

          if (r > 0) {
            if (firstBlock == null) {
              firstBlock = next;
            }

            if (r > max_rarity) {
              firstBlock = next;
              max_rarity = r;
            }
          }

          sum_rarity += r;
        }

        end_i = i - 1;

        if (sum_rarity == 0) {
          lastBlock.used_by_game = true;
        } else {
          firstBlock.used_by_game = true;
          for (int j = start_i; j <= end_i; j++) {
            Block next = blocks[j];
            if (next.rarity != 0) {
              next.used_by_game = true;
            }
          }
        }

        if (i >= numBlocks) {
          break;
        } else {
          start_i = i;
        }
      }
    }

    static class Block {
      enum BlockType {
        NULL,
        STATIC, ANIMATED,
        WALL_UP, WALL_DOWN, ROOF, SPECIAL, WALL_ANIMATED,
        SHADOW
      }

      int       rarity;
      int       zeroLine;
      BlockType type;

      boolean   conflict;
      boolean   used_by_game;

      DT1       dt1;
      int       dt1Index;
      int       blockIndex;
      DT1.Tile  tile;
      TextureRegion texture;

      Block(DT1 dt1, int dt1Index, int blockIndex, DT1.Tile tile) {
        this.dt1    = dt1;
        this.dt1Index = dt1Index;
        this.blockIndex = blockIndex;
        this.tile   = tile;
        Texture texture = new Texture(new PixmapTextureData(
            dt1.tile(blockIndex), null, false, true, false));
        //TextureFilter.Linear doesn't work with my textures...
        //texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        this.texture = new TextureRegion(texture);

        int orientation = tile.orientation;

        rarity      = tile.rarity;
        zeroLine    = -1;
        type        = BlockType.NULL;

        /*
        This is done in DT1.tile(int)
        if (orientation == 0 || orientation == 15) {
          if (tile.height != 0) {
            tile.height = -80;
          }
        } else if (orientation < 15) {
          if (tile.height != 0) {
            tile.height += 32;
          }
        }
        */

        switch (orientation) {
          case 0:
            zeroLine = 0;
            type = tile.animated == 0x01 ? BlockType.ANIMATED : BlockType.STATIC;
            break;
          case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 8: case 9:
          case 12: case 14:
            zeroLine = 0;
            type = BlockType.WALL_UP;
            break;
          case 10:
          case 11:
            zeroLine = -tile.height;
            type = BlockType.SPECIAL;
            break;
          case 13:
            zeroLine = -tile.height;
            type = BlockType.SHADOW;
            break;
          case 15:
            zeroLine = 0;
            type = BlockType.ROOF;
            break;
          default:
            assert orientation > 15;
            zeroLine = tile.height + 96;
            type = BlockType.WALL_DOWN;
        }
      }
    }
  }
}
