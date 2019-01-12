package gdx.diablo.map2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

import java.util.Arrays;

import gdx.diablo.Diablo;
import gdx.diablo.codec.FontTBL;
import gdx.diablo.graphics.PaletteIndexedBatch;

public class MapRenderer {
  private static final String TAG = "MapRenderer";
  private static final boolean DEBUG          = true;
  private static final boolean DEBUG_MATH     = DEBUG && !true;
  private static final boolean DEBUG_BUFFER   = DEBUG && true;
  private static final boolean DEBUG_SUBTILE  = DEBUG && true;
  private static final boolean DEBUG_TILE     = DEBUG && true;
  private static final boolean DEBUG_CAMERA   = DEBUG && true;
  private static final boolean DEBUG_OVERSCAN = DEBUG && true;
  private static final boolean DEBUG_GRID     = DEBUG && true;
  private static final boolean DEBUG_WALKABLE = DEBUG && !true;
  private static final boolean DEBUG_SPECIAL  = DEBUG && true;

  public static boolean RENDER_DEBUG_SUBTILE  = DEBUG_SUBTILE;
  public static boolean RENDER_DEBUG_TILE     = DEBUG_TILE;
  public static boolean RENDER_DEBUG_CAMERA   = DEBUG_CAMERA;
  public static boolean RENDER_DEBUG_OVERSCAN = DEBUG_OVERSCAN;
  public static int     RENDER_DEBUG_GRID     = DEBUG_GRID ? 3 : 0;
  public static int     RENDER_DEBUG_WALKABLE = DEBUG_WALKABLE ? DS1.MAX_LAYERS + 1 : 0;
  public static boolean RENDER_DEBUG_SPECIAL  = DEBUG_SPECIAL;

  private static final Color RENDER_DEBUG_GRID_COLOR_1 = new Color(0x3f3f3f3f);
  private static final Color RENDER_DEBUG_GRID_COLOR_2 = new Color(0x7f7f7f3f);
  private static final Color RENDER_DEBUG_GRID_COLOR_3 = new Color(0x0000ff3f);
  public static int DEBUG_GRID_MODES = 3;

  PaletteIndexedBatch batch;
  OrthographicCamera  camera;
  Map map;
  Rectangle viewBounds;
  int[] viewBuffer;
  float factor = -1.0f;

  final int tileWidth      = DT1.Tile.WIDTH;
  final int tileHeight     = DT1.Tile.HEIGHT;
  final int halfTileWidth  = tileWidth >>> 1;
  final int halfTileHeight = tileHeight >>> 1;

  final int subTileWidth      = DT1.Tile.SUBTILE_WIDTH;
  final int subTileHeight     = DT1.Tile.SUBTILE_HEIGHT;
  final int halfSubTileWidth  = subTileWidth  >>> 1;
  final int halfSubTileHeight = subTileHeight >>> 1;

  // subtile index in world-space
  int x, y;

  // subtile index in tile-space
  int stx, sty;

  // subtile index in tile-space 1D
  int t;

  // pixel offset of subtile in world-space
  int spx, spy;

  // tile index in world-space
  int tx, ty;

  // pixel offset of tile in world-space
  int tpx, tpy;

  int width, height;
  int tilesX, tilesY;
  int renderWidth, renderHeight;

  // tile index of top right tile in render area
  int startX, startY;

  // tpx and tpy of startX, startY tile in world-space
  int startPx, startPy;

  private static final Color SHADOW_TINT = new Color(0, 0, 0, 0.5f);

  public MapRenderer(PaletteIndexedBatch batch, OrthographicCamera camera) {
    this.batch = batch;
    this.camera = camera;
    viewBounds = new Rectangle();
  }

  public void setMap(Map map) {
    if (this.map != map) {
      this.map = map;
    }
  }

  public void update() {
    float width  = camera.viewportWidth  * camera.zoom;
    float height = camera.viewportHeight * camera.zoom;
    float w = width  * Math.abs(camera.up.y) + height * Math.abs(camera.up.x);
    float h = height * Math.abs(camera.up.y) + width  * Math.abs(camera.up.x);
    float x = camera.position.x - w / 2;
    float y = camera.position.y - h / 2;
    viewBounds.set(x, y, width, height);
  }

  public void setFactor(float factor) {
    if (this.factor != factor) {
      this.factor = factor;
      updateViewBuffer();
    }
  }

  private void updateViewBuffer() {
    /*
    int width  = (int) viewBounds.width;
    int height = (int) viewBounds.height;
    if (factor == 0) {
      // FIXME: This isn't correct, but should be sufficiently large enough
      width  = 8 * width;
      height = 8 * height;
    } else {
      width  *= factor;
      height *= factor;
    }
    */

    updateBounds();
    // This will generate an array s.t. each cell is the length of tiles to render
    final int viewBufferLen = tilesX + tilesY - 1;
    final int viewBufferMax = tilesX * 2 - 1;
    viewBuffer = new int[viewBufferLen];
    int x, y;
    for (x = 0, y = 1; y < viewBufferMax; x++, y += 2)
      viewBuffer[x] = viewBuffer[viewBufferLen - 1 - x] = y;
    while (viewBuffer[x] == 0)
      viewBuffer[x++] = viewBufferMax;
    if (DEBUG_BUFFER) Gdx.app.debug(TAG, "viewBuffer=" + Arrays.toString(viewBuffer));
  }

  public void setPosition(int x, int y) {
    if (this.x != x || this.y != y) {
      this.x = x;
      this.y = y;
      final int halfSubTileWidth  = DT1.Tile.SUBTILE_WIDTH  >>> 1;
      final int halfSubTileHeight = DT1.Tile.SUBTILE_HEIGHT >>> 1;
      int spx =  (x * halfSubTileWidth)  - (y * halfSubTileWidth);
      int spy = -(x * halfSubTileHeight) - (y * halfSubTileHeight);
      camera.position.set(spx, spy, 0);
      runMath(x, y);
    }
  }

  public void runMath(int x, int y) {
    camera.update();
    // subtile index in tile-space
    stx = x < 0
        ? (x + 1) % DT1.Tile.SUBTILE_SIZE + (DT1.Tile.SUBTILE_SIZE - 1)
        : x % DT1.Tile.SUBTILE_SIZE;
    sty = y < 0
        ? (y + 1) % DT1.Tile.SUBTILE_SIZE + (DT1.Tile.SUBTILE_SIZE - 1)
        : y % DT1.Tile.SUBTILE_SIZE;
    t   = DT1.Tile.SUBTILE_INDEX[stx][sty];

    // pixel offset of subtile in world-space
    spx = -halfSubTileWidth  + (x * halfSubTileWidth)  - (y * halfSubTileWidth);
    spy = -halfSubTileHeight - (x * halfSubTileHeight) - (y * halfSubTileHeight);

    // tile index in world-space
    tx = x < 0
        ? ((x + 1) / DT1.Tile.SUBTILE_SIZE) - 1
        : (x / DT1.Tile.SUBTILE_SIZE);
    ty = y < 0
        ? ((y + 1) / DT1.Tile.SUBTILE_SIZE) - 1
        : (y / DT1.Tile.SUBTILE_SIZE);

    tpx = spx - DT1.Tile.SUBTILE_OFFSET[t][0];
    tpy = spy - DT1.Tile.SUBTILE_OFFSET[t][1];

    updateBounds();

    final int offX = tilesX >>> 1;
    final int offY = tilesY >>> 1;
    startX = tx + offX - offY;
    startY = ty - offX - offY;
    startPx = tpx + renderWidth / 2 - halfTileWidth;
    startPy = tpy + renderHeight / 2 - halfTileHeight;

    if (DEBUG_MATH) {
      Gdx.app.debug(TAG, String.format("(%2d,%2d){%d,%d}[%2d,%2d](%dx%d)[%dx%d] %d,%d%n",
          x, y, stx, sty, tx, ty, width, height, tilesX, tilesY, spx, spy));
    }
  }

  private void updateBounds() {
    width  = (int) camera.viewportWidth;
    height = (int) camera.viewportHeight;

    int minTilesX = ((width  + tileWidth  - 1) / tileWidth);
    int minTilesY = ((height + tileHeight - 1) / tileHeight);
    if ((minTilesX & 1) == 1) minTilesX++;
    if ((minTilesY & 1) == 1) minTilesY++;
    tilesX = minTilesX + 3; // pad width comfortably
    tilesY = minTilesY + 7; // pad height for lower walls / upper walls
    renderWidth  = tilesX * tileWidth;
    renderHeight = tilesY * tileHeight;
    assert (tilesX & 1) == 1 && (tilesY & 1) == 1;
  }

  // TODO: render will overscan image in y-axis to accommodate walls, should change to wall only instead of entire frame
  public void render(PaletteIndexedBatch batch) {
    for (int i = 0, x, y; i < DS1.MAX_LAYERS; i++) {
      int startX2 = startX;
      int startY2 = startY;
      int startPx2 = startPx;
      int startPy2 = startPy;
      for (y = 0; y < viewBuffer.length; y++) {
        int dx = startX2;
        int dy = startY2;
        int px = startPx2;
        int py = startPy2;
        int size = viewBuffer[y];
        for (x = 0; x < size; x++) {
          Map.Zone zone = map.getZone(dx, dy);
          if (zone != null) {
            //Map.Tile[][] tiles = zone.tiles[i];
            //if (tiles != null) {
              Map.Tile tile = zone.get(i, dx, dy);
              switch (i) {
                case 0:
                case 1:
                  //drawWalls(batch, tile, px, py, false);
                  drawFloors(batch, tile, px, py);
                  //batch.setBlendMode(BlendMode.SHADOW, SHADOW_TINT);
                  //drawShadows(batch, dx, dy, px, py);
                  //batch.resetBlendMode();
                  break;
                case 2:
                  break;
                case 3: case 4: case 5: case 6:
                  drawWalls(batch, tile, px, py, true);
                  break;
                default:
                  //...
              }
            //}
          }

          dx++;
          px += halfTileWidth;
          py -= halfTileHeight;
        }

        startY2++;
        if (y >= tilesX - 1) {
          startX2++;
          startPy2 -= tileHeight;
        } else {
          startX2--;
          startPx2 -= tileWidth;
        }
      }
    }
  }

  public void renderDebug(ShapeRenderer shapes) {
    if (RENDER_DEBUG_GRID > 0)
      renderDebugGrid(shapes);

    if (RENDER_DEBUG_WALKABLE > 0)
      renderDebugWalkable(shapes);

    if (RENDER_DEBUG_TILE) {
      shapes.setColor(Color.OLIVE);
      drawDiamond(shapes, tpx, tpy, DT1.Tile.WIDTH, DT1.Tile.HEIGHT);
    }

    if (RENDER_DEBUG_SUBTILE) {
      shapes.setColor(Color.WHITE);
      drawDiamond(shapes, spx, spy, DT1.Tile.SUBTILE_WIDTH, DT1.Tile.SUBTILE_HEIGHT);
    }

    if (RENDER_DEBUG_CAMERA) {
      shapes.setColor(Color.GREEN);
      shapes.rect(
          camera.position.x - camera.viewportWidth  / 2,
          camera.position.y - camera.viewportHeight / 2,
          camera.viewportWidth + 1, camera.viewportHeight + 1);
    }

    if (RENDER_DEBUG_OVERSCAN) {
      shapes.setColor(Color.GRAY);
      shapes.rect(
          tpx - renderWidth / 2 + halfTileWidth,
          tpy - renderHeight / 2 + halfTileHeight,
          renderWidth, renderHeight);
    }
  }

  public void renderDebugWalkable(ShapeRenderer shapes) {
    //if (RENDER_DEBUG_WALKABLE == DS1.MAX_LAYERS + 1) {
    //  return;
    //}

    final int[] WALKABLE_ID = {
        20, 21, 22, 23, 24,
        15, 16, 17, 18, 19,
        10, 11, 12, 13, 14,
        5, 6, 7, 8, 9,
        0, 1, 2, 3, 4
    };

    ShapeRenderer.ShapeType shapeType = shapes.getCurrentType();
    shapes.set(ShapeRenderer.ShapeType.Filled);

    int startX2 = startX;
    int startY2 = startY;
    int startPx2 = startPx;
    int startPy2 = startPy;
    int x, y;
    for (y = 0; y < viewBuffer.length; y++) {
      int dx = startX2;
      int dy = startY2;
      int px = startPx2;
      int py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(dx, dy);
        if (zone != null) {
          if (RENDER_DEBUG_WALKABLE == DS1.MAX_LAYERS + 1) {
            for (int sty = 0, t = 0; sty < DT1.Tile.SUBTILE_SIZE; sty++) {
              for (int stx = 0; stx < DT1.Tile.SUBTILE_SIZE; stx++, t++) {
                int flags = zone.flags[zone.getLocalX(dx) * DT1.Tile.SUBTILE_SIZE + stx][zone.getLocalY(dy) * DT1.Tile.SUBTILE_SIZE + sty] & 0xFF;
                if (flags == 0) continue;
                renderDebugWalkableTiles(shapes, px, py, t, flags);
              }
            }
          } else {
            //Map.Tile[][] tiles = zone.tiles[RENDER_DEBUG_WALKABLE - 1];
            //if (tiles != null) {
              Map.Tile tile = zone.get(RENDER_DEBUG_WALKABLE - 1, dx, dy);
              for (int t = 0; tile != null && tile.tile != null && t < DT1.Tile.NUM_SUBTILES; t++) {
                int flags = tile.tile.tileFlags[WALKABLE_ID[t]] & 0xFF;
                if (flags == 0) continue;
                renderDebugWalkableTiles(shapes, px, py, t, flags);
              }
            //}
          }
        }

        dx++;
        px += halfTileWidth;
        py -= halfTileHeight;
      }

      startY2++;
      if (y >= tilesX - 1) {
        startX2++;
        startPy2 -= tileHeight;
      } else {
        startX2--;
        startPx2 -= tileWidth;
      }
    }

    shapes.set(shapeType);
  }

  private void renderDebugWalkableTiles(ShapeRenderer shapes, int px, int py, int t, int flags) {
    int offX = px + DT1.Tile.SUBTILE_OFFSET[t][0];
    int offY = py + DT1.Tile.SUBTILE_OFFSET[t][1];

    shapes.setColor(Color.CORAL);
    drawDiamond(shapes, offX, offY, DT1.Tile.SUBTILE_WIDTH, DT1.Tile.SUBTILE_HEIGHT);

    offY += halfSubTileHeight;

    if ((flags & DT1.Tile.FLAG_BLOCK_WALK) != 0) {
      shapes.setColor(Color.FIREBRICK);
      shapes.triangle(
          offX + 16, offY,
          offX + 16, offY + 8,
          offX + 24, offY + 4);
    }
    if ((flags & DT1.Tile.FLAG_BLOCK_LIGHT_LOS) != 0) {
      shapes.setColor(Color.FOREST);
      shapes.triangle(
          offX + 16, offY,
          offX + 32, offY,
          offX + 24, offY + 4);
    }
    if ((flags & DT1.Tile.FLAG_BLOCK_JUMP) != 0) {
      shapes.setColor(Color.ROYAL);
      shapes.triangle(
          offX + 16, offY,
          offX + 32, offY,
          offX + 24, offY - 4);
    }
    if ((flags & DT1.Tile.FLAG_BLOCK_PLAYER_WALK) != 0) {
      shapes.setColor(Color.VIOLET);
      shapes.triangle(
          offX + 16, offY,
          offX + 16, offY - 8,
          offX + 24, offY - 4);
    }
    if ((flags & DT1.Tile.FLAG_BLOCK_UNKNOWN1) != 0) {
      shapes.setColor(Color.GOLD);
      shapes.triangle(
          offX + 16, offY,
          offX + 16, offY - 8,
          offX + 8, offY - 4);
    }
    if ((flags & DT1.Tile.FLAG_BLOCK_LIGHT) != 0) {
      shapes.setColor(Color.SKY);
      shapes.triangle(
          offX, offY,
          offX + 16, offY,
          offX + 8, offY - 4);
    }
    if ((flags & DT1.Tile.FLAG_BLOCK_UNKNOWN2) != 0) {
      shapes.setColor(Color.WHITE);
      shapes.triangle(
          offX, offY,
          offX + 16, offY,
          offX + 8, offY + 4);
    }
    if ((flags & DT1.Tile.FLAG_BLOCK_UNKNOWN3) != 0) {
      shapes.setColor(Color.SLATE);
      shapes.triangle(
          offX + 16, offY,
          offX + 16, offY + 8,
          offX + 8, offY + 4);
    }
  }

  public void renderDebugGrid(ShapeRenderer shapes) {
    int x, y;
    switch (RENDER_DEBUG_GRID) {
      case 1:
        shapes.setColor(RENDER_DEBUG_GRID_COLOR_1);
        int startPx2 = startPx;
        int startPy2 = startPy;
        for (y = 0; y < viewBuffer.length; y++) {
          int px = startPx2;
          int py = startPy2;
          int size = viewBuffer[y];
          for (x = 0; x < size; x++) {
            for (int t = 0; t < DT1.Tile.NUM_SUBTILES; t++) {
              drawDiamond(shapes,
                  px + DT1.Tile.SUBTILE_OFFSET[t][0], py + DT1.Tile.SUBTILE_OFFSET[t][1],
                  DT1.Tile.SUBTILE_WIDTH, DT1.Tile.SUBTILE_HEIGHT);
            }

            px += halfTileWidth;
            py -= halfTileHeight;
          }

          if (y >= tilesX - 1) {
            startPy2 -= tileHeight;
          } else {
            startPx2 -= tileWidth;
          }
        }

      case 2:
        shapes.setColor(RENDER_DEBUG_GRID_COLOR_2);
        startPx2 = startPx;
        startPy2 = startPy;
        for (y = 0; y < viewBuffer.length; y++) {
          int px = startPx2;
          int py = startPy2;
          int size = viewBuffer[y];
          for (x = 0; x < size; x++) {
            drawDiamond(shapes, px, py, DT1.Tile.WIDTH, DT1.Tile.HEIGHT);
            px += halfTileWidth;
            py -= halfTileHeight;
          }

          if (y >= tilesX - 1) {
            startPy2 -= tileHeight;
          } else {
            startPx2 -= tileWidth;
          }
        }

      case 3:
        shapes.setColor(RENDER_DEBUG_GRID_COLOR_3);
        ShapeRenderer.ShapeType shapeType = shapes.getCurrentType();
        shapes.set(ShapeRenderer.ShapeType.Filled);
        final int LINE_WIDTH = 2;
        int startX2 = startX;
        int startY2 = startY;
        startPx2 = startPx;
        startPy2 = startPy;
        for (y = 0; y < viewBuffer.length; y++) {
          int dx = startX2;
          int dy = startY2;
          int px = startPx2;
          int py = startPy2;
          int size = viewBuffer[y];
          for (x = 0; x < size; x++) {
            Map.Zone zone = map.getZone(dx, dy);
            if (zone != null) {
              int mod = dx < 0
                  ? (dx + 1) % zone.gridSizeX + (zone.gridSizeX - 1)
                  : dx % zone.gridSizeX;
              if (mod == 0)
                shapes.rectLine(px, py + halfTileHeight, px + halfTileWidth, py + tileHeight, LINE_WIDTH);
              else if (mod == zone.gridSizeX - 1)
                shapes.rectLine(px + tileWidth, py + halfTileHeight, px + halfTileWidth, py, LINE_WIDTH);

              mod = dy < 0
                  ? (dy + 1) % zone.gridSizeY + (zone.gridSizeY - 1)
                  : dy % zone.gridSizeY;
              if (mod == 0)
                shapes.rectLine(px + halfTileWidth, py + tileHeight, px + tileWidth, py + halfTileHeight, LINE_WIDTH);
              else if (mod == zone.gridSizeY - 1)
                shapes.rectLine(px + halfTileWidth, py, px, py + halfTileHeight, LINE_WIDTH);
            }

            dx++;
            px += halfTileWidth;
            py -= halfTileHeight;
          }

          startY2++;
          if (y >= tilesX - 1) {
            startX2++;
            startPy2 -= tileHeight;
          } else {
            startX2--;
            startPx2 -= tileWidth;
          }
        }
        shapes.set(shapeType);

      default:
    }
  }

  private static void drawDiamond(ShapeRenderer shapes, float x, float y, int width, int height) {
    int hw = width  >>> 1;
    int hh = height >>> 1;
    shapes.line(x        , y + hh    , x + hw   , y + height);
    shapes.line(x + hw   , y + height, x + width, y + hh    );
    shapes.line(x + width, y + hh    , x + hw   , y         );
    shapes.line(x + hw   , y         , x        , y + hh    );
  }

  private void drawFloors(PaletteIndexedBatch batch, Map.Tile tile, int px, int py) {
    if (tile == null) {
      return;
    }

    batch.draw(tile.tile.texture, px, py, tile.tile.texture.getWidth() + 1, tile.tile.texture.getHeight() + 1);
  }

  private void drawWalls(PaletteIndexedBatch batch, Map.Tile tile, int px, int py, boolean upper) {
    if (tile == null) {
      return;
    }

    //RENDER_DEBUG_SPECIAL
    if (Orientation.isSpecial(tile.cell.orientation)) {
      if (!RENDER_DEBUG_SPECIAL) return;
      FontTBL.BitmapFont font = Diablo.fonts.font16;
      GlyphLayout layout = new GlyphLayout(font, map.getSpecialName(tile.cell.id));
      font.draw(batch, layout,
          px + DT1.Tile.WIDTH / 2 - layout.width / 2,
          py + DT1.Tile.HEIGHT / 2 - layout.height / 2);
      return;
    }

    if (tile.tile.orientation < Orientation.LEFT_WALL) {
      return;
    }

    batch.draw(tile.tile.texture, px, tile.tile.orientation == Orientation.ROOF ? py + tile.tile.roofHeight : py);
    if (tile.tile.orientation == Orientation.RIGHT_NORTH_CORNER_WALL) {
      DT1.Tile sister = Diablo.dt1s.get(Orientation.LEFT_NORTH_CORNER_WALL, tile.tile.mainIndex, tile.tile.subIndex);
      batch.draw(sister.texture, px, py);
    }
  }
}
