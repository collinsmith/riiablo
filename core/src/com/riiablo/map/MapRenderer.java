package com.riiablo.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.riiablo.Riiablo;
import com.riiablo.entity.Engine;
import com.riiablo.entity.Entity;
import com.riiablo.entity.Object;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DT1.Tile;
import com.riiablo.util.DebugUtils;
import com.riiablo.util.EngineUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public class MapRenderer {
  private static final String TAG = "MapRenderer";
  private static final boolean DEBUG          = true;
  private static final boolean DEBUG_MATH     = DEBUG && !true;
  private static final boolean DEBUG_BUFFER   = DEBUG && true;
  private static final boolean DEBUG_SUBTILE  = DEBUG && true;
  private static final boolean DEBUG_TILE     = DEBUG && !true;
  private static final boolean DEBUG_CAMERA   = DEBUG && true;
  private static final boolean DEBUG_OVERSCAN = DEBUG && true;
  private static final boolean DEBUG_GRID     = DEBUG && true;
  private static final boolean DEBUG_WALKABLE = DEBUG && !true;
  private static final boolean DEBUG_SPECIAL  = DEBUG && true;
  private static final boolean DEBUG_MOUSE    = DEBUG && true;
  private static final boolean DEBUG_PATHS    = DEBUG && !true;
  private static final boolean DEBUG_POPPADS  = DEBUG && !true;
  private static final boolean DEBUG_ENTITIES = DEBUG && true;

  public static boolean RENDER_DEBUG_SUBTILE  = DEBUG_SUBTILE;
  public static boolean RENDER_DEBUG_TILE     = DEBUG_TILE;
  public static boolean RENDER_DEBUG_CAMERA   = DEBUG_CAMERA;
  public static boolean RENDER_DEBUG_OVERSCAN = DEBUG_OVERSCAN;
  public static int     RENDER_DEBUG_GRID     = DEBUG_GRID ? 3 : 0;
  public static int     RENDER_DEBUG_WALKABLE = DEBUG_WALKABLE ? 1 : 0;
  public static boolean RENDER_DEBUG_SPECIAL  = DEBUG_SPECIAL;
  public static boolean RENDER_DEBUG_PATHS    = DEBUG_PATHS;

  private static final Color RENDER_DEBUG_GRID_COLOR_1 = new Color(0x3f3f3f3f);
  private static final Color RENDER_DEBUG_GRID_COLOR_2 = new Color(0x7f7f7f3f);
  private static final Color RENDER_DEBUG_GRID_COLOR_3 = new Color(0x0000ff3f);
  public static int DEBUG_GRID_MODES = 3;

  // Extra padding to ensure proper overscan, should be odd value
  private static final int TILES_PADDING_X = 3;
  private static final int TILES_PADDING_Y = 7;

  private static final Comparator<Entity> SUBTILE_ORDER = new Comparator<Entity>() {
    @Override
    public int compare(Entity e1, Entity e2) {
      Vector2 pos1 = e1.position();
      Vector2 pos2 = e2.position();
      int i = Float.compare(pos1.y, pos2.y);
      return i == 0 ? Float.compare(pos1.x, pos2.x): i;
    }
  };

  private final Vector3    tmpVec3  = new Vector3();
  private final Vector2    tmpVec2a = new Vector2();
  private final Vector2    tmpVec2b = new Vector2();
  private final GridPoint2 tmpVec2i = new GridPoint2();

  PaletteIndexedBatch batch;
  OrthographicCamera  camera;
  Map                 map;
  int                 viewBuffer[];
  Array<Entity>       cache[][][];

  Entity  src;
  Vector2 currentPos = new Vector2();

  // sub-tile index in world-space
  int x, y;

  // sub-tile index in tile-space 2-D
  int stx, sty;

  // sub-tile index in tile-space 1-D
  int t;

  // pixel offset of sub-tile in world-space
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

  // camera bounds
  int renderMinX, renderMinY;
  int renderMaxX, renderMaxY;

  // DT1 mainIndexes to not draw
  final Bits popped = new Bits();

  Engine entities;
  final Array<Entity> nearbyEntities = new Array<>();

  public MapRenderer(PaletteIndexedBatch batch, float viewportWidth, float viewportHeight) {
    this.batch  = batch;
    this.camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    camera.setToOrtho(false, viewportWidth, viewportHeight);
    setClipPlane(-1000, 1000);
  }

  // This adjusts clip plane for debugging purposes (some elements rotated to map grid)
  private void setClipPlane(float near, float far) {
    camera.near = near;
    camera.far  = far;
    camera.update();
  }

  public Map getMap() {
    return map;
  }

  public void setMap(Map map) {
    if (this.map != map) {
      this.map = map;
    }
  }

  public Entity getSrc() {
    return src;
  }

  public void setSrc(Entity src) {
    if (this.src != src) {
      this.src = src;
    }
  }

  public void setEntities(Engine entities) {
    this.entities = entities;
  }

  public Array<Entity> getNearbyEntities() {
    return nearbyEntities;
  }

  public float zoom() {
    return camera.zoom;
  }

  public void zoom(float zoom) {
    zoom(zoom, false);
  }

  public void zoom(float zoom, boolean resize) {
    if (camera.zoom != zoom) {
      camera.zoom = zoom;
      update(true);
      if (resize) resize();
    }
  }

  // FIXME: result is slightly off -- more obvious with higher/lower zooms
  //        possibly caused by trying to use the projection in Diablo.viewport.camera space
  public Vector2 projectScaled(Vector2 dst) {
    tmpVec3.set(dst.x, dst.y, 0);
    camera.project(tmpVec3, 0, 0, camera.viewportWidth, camera.viewportHeight);
    return dst.set(tmpVec3.x, tmpVec3.y);
  }

  public Vector2 project(Vector2 dst) {
    return project(dst.x, dst.y, dst);
  }

  public Vector2 project(float x, float y, Vector2 dst) {
    return EngineUtils.worldToScreenCoords(x, y, dst);
  }

  public Vector2 unproject(Vector2 dst) {
    return unproject(dst.x, dst.y, dst);
  }

  public Vector2 unproject(float x, float y) {
    return unproject(x, y, new Vector2());
  }

  public Vector2 unproject(float x, float y, Vector2 dst) {
    tmpVec3.set(x, y, 0);
    camera.unproject(tmpVec3);
    return dst.set(tmpVec3.x, tmpVec3.y);
  }

  public GridPoint2 coords() {
    return coords(new GridPoint2());
  }

  public GridPoint2 coords(GridPoint2 dst) {
    tmpVec2a.set(Gdx.input.getX(), Gdx.input.getY());
    unproject(tmpVec2a);
    return coords(tmpVec2a.x, tmpVec2a.y, dst);
  }

  public GridPoint2 coords(float x, float y) {
    return coords(x, y, new GridPoint2());
  }

  public GridPoint2 coords(float x, float y, GridPoint2 dst) {
    float adjustX = (int) x;
    float adjustY = (int) y - Tile.SUBTILE_HEIGHT50;
    float selectX = ( adjustX / Tile.SUBTILE_WIDTH50 - adjustY / Tile.SUBTILE_HEIGHT50) / 2;
    float selectY = (-adjustX / Tile.SUBTILE_WIDTH50 - adjustY / Tile.SUBTILE_HEIGHT50) / 2;
    if (selectX < 0) selectX--;
    if (selectY < 0) selectY--;
    return dst.set((int) selectX, (int) selectY);
  }

  public float angle(Vector2 src, Vector2 dst) {
    project(tmpVec2a.set(src));
    project(tmpVec2b.set(dst));
    tmpVec2b.sub(tmpVec2a);
    return MathUtils.atan2(tmpVec2b.y, tmpVec2b.x);
  }

  @SuppressWarnings("unchecked")
  public void resize() {
    updateBounds();
    final int viewBufferLen = tilesX + tilesY - 1;
    final int viewBufferMax = tilesX * 2 - 1; // FIXME: double check when adding support for other aspect ratios, need a ternary operation
    viewBuffer = new int[viewBufferLen];
    int x, y;
    for (x = 0, y = 1; y < viewBufferMax; x++, y += 2)
      viewBuffer[x] = viewBuffer[viewBufferLen - 1 - x] = y;
    while (viewBuffer[x] == 0)
      viewBuffer[x++] = viewBufferMax;
    if (DEBUG_BUFFER) {
      int len = 0;
      for (int i : viewBuffer) len += i;
      Gdx.app.debug(TAG, "viewBuffer[" + len + "]=" + Arrays.toString(viewBuffer));
    }

    cache = new Array[viewBufferLen][][];
    for (int i = 0; i < viewBufferLen; i++) {
      int viewBufferRun = viewBuffer[i];
      cache[i] = new Array[viewBufferRun][];
      for (int j = 0; j < viewBufferRun; j++) {
        cache[i][j] = new Array[] {
            new Array<Entity>(Tile.NUM_SUBTILES), // TODO: Really {@code (Tile.SUBTILE_SIZE - 1) * (Tile.SUBTILE_SIZE - 1)}
            new Array<Entity>(1), // better size TBD
            new Array<Entity>(Tile.SUBTILE_SIZE + Tile.SUBTILE_SIZE - 1), // only upper walls
        };
      }
    }
  }

  private void updateBounds() {
    width  = (int) (camera.viewportWidth  * camera.zoom);
    height = (int) (camera.viewportHeight * camera.zoom);

    renderMinX = (int) camera.position.x - (width  >>> 1);
    renderMinY = (int) camera.position.y - (height >>> 1);
    renderMaxX = renderMinX + width;
    renderMaxY = renderMinY + height;

    int minTilesX = ((width  + Tile.WIDTH  - 1) / Tile.WIDTH);
    int minTilesY = ((height + Tile.HEIGHT - 1) / Tile.HEIGHT);
    if ((minTilesX & 1) == 1) minTilesX++;
    if ((minTilesY & 1) == 1) minTilesY++;
    tilesX = minTilesX + TILES_PADDING_X;
    tilesY = minTilesY + TILES_PADDING_Y;
    renderWidth  = tilesX * Tile.WIDTH;
    renderHeight = tilesY * Tile.HEIGHT;
    assert (tilesX & 1) == 1 && (tilesY & 1) == 1;
  }

  public void update() {
    update(false);
  }

  public void update(boolean force) {
    if (src == null) return;
    Vector2 pos = src.position();
    if (pos.epsilonEquals(currentPos) && !force) return;
    currentPos.set(pos);
    this.x = Map.round(pos.x);//(int) pos.x;
    this.y = Map.round(pos.y);//(int) pos.y;
    EngineUtils.worldToScreenCoords(pos, tmpVec2a);
    camera.position.set(tmpVec2a, 0);
    camera.update();

    // subtile index in tile-space
    stx = x < 0
        ? (x + 1) % Tile.SUBTILE_SIZE + (Tile.SUBTILE_SIZE - 1)
        : x % Tile.SUBTILE_SIZE;
    sty = y < 0
        ? (y + 1) % Tile.SUBTILE_SIZE + (Tile.SUBTILE_SIZE - 1)
        : y % Tile.SUBTILE_SIZE;
    t   = Tile.SUBTILE_INDEX[stx][sty];

    // pixel offset of subtile in world-space
    EngineUtils.worldToScreenCoords(x, y, tmpVec2a).sub(Tile.SUBTILE_WIDTH50, Tile.SUBTILE_HEIGHT50);
    spx = (int) tmpVec2a.x;
    spy = (int) tmpVec2a.y;

    // tile index in world-space
    tx = x < 0
        ? ((x + 1) / Tile.SUBTILE_SIZE) - 1
        : (x / Tile.SUBTILE_SIZE);
    ty = y < 0
        ? ((y + 1) / Tile.SUBTILE_SIZE) - 1
        : (y / Tile.SUBTILE_SIZE);

    tpx = spx - Tile.SUBTILE_OFFSET[t][0];
    tpy = spy - Tile.SUBTILE_OFFSET[t][1];

    //updateBounds();
    renderMinX = (int) camera.position.x - (width  >>> 1);
    renderMinY = (int) camera.position.y - (height >>> 1);
    renderMaxX = renderMinX + width;
    renderMaxY = renderMinY + height;

    final int offX = tilesX >>> 1;
    final int offY = tilesY >>> 1;
    startX = tx + offX - offY;
    startY = ty - offX - offY;
    startPx = tpx + renderWidth  / 2 - Tile.WIDTH50;
    startPy = tpy + renderHeight / 2 - Tile.HEIGHT50;

    if (DEBUG_MATH) {
      Gdx.app.debug(TAG,
          String.format("(%2d,%2d){%d,%d}[%2d,%2d](%dx%d)[%dx%d] %d,%d",
              x, y, stx, sty, tx, ty, width, height, tilesX, tilesY, spx, spy));
    }

    map.updatePopPads(popped, x, y, tx, ty, stx, sty);
    if (DEBUG_POPPADS) {
      String popPads = getPopPads();
      if (!popPads.isEmpty()) Gdx.app.debug(TAG, "PopPad IDs: " + popPads);
    }
  }

  private String getPopPads() {
    StringBuilder builder = new StringBuilder();
    for (int i = popped.nextSetBit(0); i >= 0; i = popped.nextSetBit(i + 1)) {
      builder.append(i).append(',');
    }

    if (builder.length() > 0) {
      builder.setLength(builder.length() - 1);
    }

    return builder.toString();
  }

  private void updateEntities(float delta) {
    for (Map.Zone zone : new Array.ArrayIterator<>(map.zones)) {
      for (Entity entity : zone.entities) {
        entity.update(delta);
        entity.act(delta);
      }
    }
    if (entities != null) {
      for (Entity entity : entities) {
        entity.update(delta);
        entity.act(delta);
      }
    }
  }

  public void prepare(PaletteIndexedBatch batch) {
    batch.setProjectionMatrix(camera.combined);
  }

  public void draw(float delta) {
    prepare(batch);
    updateEntities(delta);
    buildCaches();
    drawBackground();
    drawForeground();
  }

  /**
   * TODO: This is still a fairly expensive calculation because it needs to read all entities
   *       viewbuffer size times -- this can be sped up using some kind of cache within entities
   *       themselves (so each time position changes, update viewbuffer cache position) or by
   *       storing the entity at its position in array within the zone (similar to how the collision
   *       map works -- this may very well be how the actual game works, but some spaces might allow
   *       more than 1 entity, e.g., player + item, or monsters that don't have collision -- I'll
   *       look into this more when I add entity collision detection.
   */
  void buildCaches() {
    int x, y;
    int startX2 = startX;
    int startY2 = startY;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) buildCache(cache[y][x], zone, stx, sty);
        tx++;
        stx += Tile.SUBTILE_SIZE;
      }

      startY2++;
      if (y >= tilesX - 1) {
        startX2++;
      } else {
        startX2--;
      }
    }

    nearbyEntities.size = 0;
    for (y = 0; y < viewBuffer.length; y++) {
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        nearbyEntities.addAll(cache[y][x][0]);
        nearbyEntities.addAll(cache[y][x][1]);
        nearbyEntities.addAll(cache[y][x][2]);
      }
    }
  }

  void buildCache(Array<Entity>[] cache, Map.Zone zone, int stx, int sty) {
    cache[0].size = cache[1].size = cache[2].size = 0;
    int orderFlag;
    for (Entity entity : zone.entities) {
      Vector2 pos = entity.position();
      if ((stx <= pos.x && pos.x < stx + Tile.SUBTILE_SIZE)
       && (sty <= pos.y && pos.y < sty + Tile.SUBTILE_SIZE)) {
        if ((entity instanceof Object)) {
          orderFlag = ((Object) entity).getOrderFlag();
        } else {
          orderFlag = stx == pos.x || sty == pos.y ? 2 : 0;
        }

        cache[orderFlag].add(entity);
      }
    }
    if (entities != null) {
      for (Entity entity : entities) {
        Vector2 pos = entity.position();
        if ((stx <= pos.x && pos.x < stx + Tile.SUBTILE_SIZE)
         && (sty <= pos.y && pos.y < sty + Tile.SUBTILE_SIZE)) {
          cache[stx == pos.x || sty == pos.y ? 2 : 0].add(entity);
        }
      }
    }
    cache[0].sort(SUBTILE_ORDER);
    cache[1].sort(SUBTILE_ORDER);
    cache[2].sort(SUBTILE_ORDER);
  }

  void drawBackground() {
    int x, y;
    int startX2 = startX;
    int startY2 = startY;
    int startPx2 = startPx;
    int startPy2 = startPy;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      int px = startPx2;
      int py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) {
          drawLowerWalls(batch, zone, tx, ty, px, py);
          drawFloors(batch, zone, tx, ty, px, py);
        }

        tx++;
        stx += Tile.SUBTILE_SIZE;
        px += Tile.WIDTH50;
        py -= Tile.HEIGHT50;
      }

      startY2++;
      if (y >= tilesX - 1) {
        startX2++;
        startPy2 -= Tile.HEIGHT;
      } else {
        startX2--;
        startPx2 -= Tile.WIDTH;
      }
    }

    startX2 = startX;
    startY2 = startY;
    startPx2 = startPx;
    startPy2 = startPy;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      int px = startPx2;
      int py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) {
          //buildCaches(zone, stx, sty);
          drawShadows(batch, zone, tx, ty, px, py, cache[y][x]);
        }

        tx++;
        stx += Tile.SUBTILE_SIZE;
        px += Tile.WIDTH50;
        py -= Tile.HEIGHT50;
      }

      startY2++;
      if (y >= tilesX - 1) {
        startX2++;
        startPy2 -= Tile.HEIGHT;
      } else {
        startX2--;
        startPx2 -= Tile.WIDTH;
      }
    }
  }

  void drawForeground() {
    int x, y;
    int startX2 = startX;
    int startY2 = startY;
    int startPx2 = startPx;
    int startPy2 = startPy;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      int px = startPx2;
      int py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) {
          //buildCaches(zone, stx, sty);
          Array<Entity>[] cache = this.cache[y][x];
          drawEntities(cache, 1); // floors
          drawEntities(cache, 2); // walls/doors
          drawWalls(batch, zone, tx, ty, px, py);
          //drawWalls (trees and maybe columns?)
          drawEntities(cache, 0); // objects
          drawRoofs(batch, zone, tx, ty, px, py);
        }

        tx++;
        stx += Tile.SUBTILE_SIZE;
        px += Tile.WIDTH50;
        py -= Tile.HEIGHT50;
      }

      startY2++;
      if (y >= tilesX - 1) {
        startX2++;
        startPy2 -= Tile.HEIGHT;
      } else {
        startX2--;
        startPx2 -= Tile.WIDTH;
      }
    }
  }

  void drawEntities(Array<Entity>[] cache, int i) {
    for (Entity entity : cache[i]) {
      if (!entity.target().isZero() && !entity.position().epsilonEquals(entity.target())) {
        entity.angle(angle(entity.position(), entity.target()));
      }

      entity.draw(batch);
    }
  }

  void drawLowerWalls(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, int px, int py) {
    if (px > renderMaxX || py > renderMaxY || px + Tile.WIDTH < renderMinX) return;
    for (int i = Map.WALL_OFFSET; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      Map.Tile tile = zone.get(i, tx, ty);
      if (tile == null || tile.tile == null) continue;
      switch (tile.tile.orientation) {
        case Orientation.LOWER_LEFT_WALL:
        case Orientation.LOWER_RIGHT_WALL:
        case Orientation.LOWER_NORTH_CORNER_WALL:
        case Orientation.LOWER_SOUTH_CORNER_WALL:
          batch.draw(tile.tile.texture, px, py);
          // fall-through to continue
        default:
      }
    }
  }

  void drawFloors(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, int px, int py) {
    if (px > renderMaxX || py > renderMaxY) return;
    if (px + Tile.WIDTH < renderMinX || py + Tile.HEIGHT < renderMinY) return;
    for (int i = Map.FLOOR_OFFSET; i < Map.FLOOR_OFFSET + Map.MAX_FLOORS; i++) {
      Map.Tile tile = zone.get(i, tx, ty);
      if (tile == null) continue;
      TextureRegion texture;
      int subst = tile.cell != null ? map.warpSubsts.get(tile.cell.id, -1) : -1;
      if (subst != -1) { // TODO: Performance can be improved if the reference is updated to below subst
        texture = map.dt1s.get(zone.level.LevelType).get(subst).texture;
      } else {
        texture = tile.tile.texture;
      }
      //if (texture.getTexture().getTextureObjectHandle() == 0) return;
      batch.draw(texture, px, py, texture.getRegionWidth(), texture.getRegionHeight() + 1);
    }
  }

  void drawShadows(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, int px, int py, Array<Entity>[] cache) {
    batch.setBlendMode(BlendMode.SOLID, Riiablo.colors.modal75);
    for (int i = Map.SHADOW_OFFSET; i < Map.SHADOW_OFFSET + Map.MAX_SHADOWS; i++) {
      Map.Tile tile = zone.get(i, tx, ty);
      if (tile == null) continue;
      if (px > renderMaxX || px + Tile.WIDTH  < renderMinX) continue;
      TextureRegion texture = tile.tile.texture;
      if (py > renderMaxY || py + texture.getRegionHeight() < renderMinY) continue;
      batch.draw(texture, px, py, texture.getRegionWidth(), texture.getRegionHeight());
    }
    /*
    for (int i = Map.WALL_OFFSET; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      Map.Tile tile = zone.get(i, tx, ty);
      if (tile == null || tile.tile == null) continue;
      if (tile.tile.orientation != Orientation.SHADOW) continue;
      TextureRegion texture = tile.tile.texture;
      batch.draw(texture, px, py, texture.getRegionWidth(), texture.getRegionHeight());
    }
    */
    for (Array<Entity> c : cache) {
      for (Entity entity : c) entity.drawShadow(batch);
    }
    batch.resetBlendMode();
  }

  void drawWalls(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, int px, int py) {
    if (px > renderMaxX || py > renderMaxY || px + Tile.WIDTH < renderMinX) return;
    for (int i = Map.WALL_OFFSET; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      Map.Tile tile = zone.get(i, tx, ty);
      if (tile == null || tile.tile == null) continue;
      //if (popped.get(tile.tile.mainIndex)) continue;
      switch (tile.tile.orientation) {
        case Orientation.LEFT_WALL:
        case Orientation.LEFT_NORTH_CORNER_WALL:
        case Orientation.LEFT_END_WALL:
        case Orientation.LEFT_WALL_DOOR:
          //break;
        case Orientation.RIGHT_WALL:
        case Orientation.RIGHT_NORTH_CORNER_WALL:
        case Orientation.RIGHT_END_WALL:
        case Orientation.RIGHT_WALL_DOOR:
          //break;
        case Orientation.SOUTH_CORNER_WALL:
        case Orientation.PILLAR:
          /**
           * TODO: pseudocode: if TREE, assume only 1 tile in all 25 subtiles is unwalkable and use
           *       that position as the tile position and render it as if its an entity
           */
        case Orientation.TREE: // TODO: should be in-line rendered with entities
          if (py + tile.tile.texture.getRegionHeight() < renderMinY) break;
          batch.draw(tile.tile.texture, px, py);
          if (tile.tile.orientation == Orientation.RIGHT_NORTH_CORNER_WALL) {
            batch.draw(tile.sibling.texture, px, py);
          }
          // fall-through to continue
        default:
      }
    }
  }

  void drawRoofs(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, int px, int py) {
    if (px > renderMaxX || px + Tile.WIDTH < renderMinX) return;
    for (int i = Map.WALL_OFFSET; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      Map.Tile tile = zone.get(i, tx, ty);
      if (tile == null || tile.tile == null) continue;
      if (popped.get(tile.tile.mainIndex)) continue;
      if (!Orientation.isRoof(tile.tile.orientation)) continue;
      if (py + tile.tile.roofHeight > renderMaxY) continue;
      if (py + tile.tile.roofHeight + tile.tile.texture.getRegionHeight() < renderMinY) continue;
      batch.draw(tile.tile.texture, px, py + tile.tile.roofHeight);
    }
  }

  public void drawDebug(ShapeRenderer shapes) {
    batch.setProjectionMatrix(camera.combined);
    shapes.setProjectionMatrix(camera.combined);
    if (RENDER_DEBUG_GRID > 0)
      drawDebugGrid(shapes);

    if (RENDER_DEBUG_WALKABLE > 0)
      drawDebugWalkable(shapes);

    if (RENDER_DEBUG_SPECIAL)
      drawDebugSpecial(shapes);

    if (RENDER_DEBUG_TILE) {
      shapes.setColor(Color.OLIVE);
      DebugUtils.drawDiamond2(shapes, tpx, tpy, Tile.WIDTH, Tile.HEIGHT);
    }

    if (RENDER_DEBUG_SUBTILE) {
      shapes.setColor(Color.WHITE);
      DebugUtils.drawDiamond2(shapes, spx, spy, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
    }

    if (DEBUG_ENTITIES)
      drawDebugObjects(batch, shapes);

    if (RENDER_DEBUG_PATHS)
      drawDebugPaths(batch, shapes);

    if (RENDER_DEBUG_CAMERA) {
      float viewportWidth  = width;
      float viewportHeight = height;
      shapes.setColor(Color.GREEN);
      shapes.rect(
          camera.position.x - MathUtils.ceil(viewportWidth  / 2) - 1,
          camera.position.y - MathUtils.ceil(viewportHeight / 2) - 1,
          viewportWidth + 2, viewportHeight + 2);
    }

    if (RENDER_DEBUG_OVERSCAN) {
      /*shapes.setColor(Color.LIGHT_GRAY);
      shapes.rect(
          tpx - renderWidth  / 2 + Tile.WIDTH50,
          tpy - renderHeight / 2 + Tile.HEIGHT50 + renderHeight,
          renderWidth, 96);*/
      shapes.setColor(Color.GRAY);
      shapes.rect(
          tpx - renderWidth  / 2 + Tile.WIDTH50,
          tpy - renderHeight / 2 + Tile.HEIGHT50,
          renderWidth, renderHeight);
      /*shapes.setColor(Color.DARK_GRAY);
      shapes.rect(
          tpx - renderWidth  / 2 + Tile.WIDTH50,
          tpy - renderHeight / 2 + Tile.HEIGHT50 - 96,
          renderWidth, 96);*/
    }

    if (DEBUG_MOUSE) {
      coords(tmpVec2i);
      EngineUtils.worldToScreenCoords(tmpVec2i.x, tmpVec2i.y, tmpVec2i).sub(Tile.SUBTILE_WIDTH50, Tile.SUBTILE_HEIGHT50);

      shapes.setColor(Color.VIOLET);
      DebugUtils.drawDiamond2(shapes, tmpVec2i.x, tmpVec2i.y, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
    }
  }

  private void drawDebugGrid(ShapeRenderer shapes) {
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
            for (int t = 0; t < Tile.NUM_SUBTILES; t++) {
              DebugUtils.drawDiamond2(shapes,
                  px + Tile.SUBTILE_OFFSET[t][0], py + Tile.SUBTILE_OFFSET[t][1],
                  Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
            }

            px += Tile.WIDTH50;
            py -= Tile.HEIGHT50;
          }

          if (y >= tilesX - 1) {
            startPy2 -= Tile.HEIGHT;
          } else {
            startPx2 -= Tile.WIDTH;
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
            DebugUtils.drawDiamond2(shapes, px, py, Tile.WIDTH, Tile.HEIGHT);
            px += Tile.WIDTH50;
            py -= Tile.HEIGHT50;
          }

          if (y >= tilesX - 1) {
            startPy2 -= Tile.HEIGHT;
          } else {
            startPx2 -= Tile.WIDTH;
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
          int tx = startX2;
          int ty = startY2;
          int px = startPx2;
          int py = startPy2;
          int size = viewBuffer[y];
          for (x = 0; x < size; x++) {
            Map.Zone zone = map.getZone(tx * Tile.SUBTILE_SIZE, ty * Tile.SUBTILE_SIZE);
            if (zone != null) {
              int localTX = zone.getLocalTX(tx);
              int modX = localTX < 0
                  ? (localTX + 1) % zone.gridSizeX + (zone.gridSizeX - 1)
                  : localTX % zone.gridSizeX;
              if (modX == 0)
                shapes.rectLine(px, py + Tile.HEIGHT50, px + Tile.WIDTH50, py + Tile.HEIGHT, LINE_WIDTH);
              else if (modX == zone.gridSizeX - 1)
                shapes.rectLine(px + Tile.WIDTH, py + Tile.HEIGHT50, px + Tile.WIDTH50, py, LINE_WIDTH);

              int localTY = zone.getLocalTY(ty);
              int modY = localTY < 0
                  ? (localTY + 1) % zone.gridSizeY + (zone.gridSizeY - 1)
                  : localTY % zone.gridSizeY;
              if (modY == 0)
                shapes.rectLine(px + Tile.WIDTH50, py + Tile.HEIGHT, px + Tile.WIDTH, py + Tile.HEIGHT50, LINE_WIDTH);
              else if (modY == zone.gridSizeY - 1)
                shapes.rectLine(px + Tile.WIDTH50, py, px, py + Tile.HEIGHT50, LINE_WIDTH);

              if (modX == 0 && modY == 0) {
                Map.Preset preset = zone.getGrid(tx, ty);
                StringBuilder sb = new StringBuilder(tx + "," + ty);
                if (preset != null)
                  sb.append('\n').append(preset.ds1Path);
                String desc = sb.toString();

                shapes.end();
                batch.getProjectionMatrix()
                    .translate(px + Tile.WIDTH50, py + Tile.HEIGHT - Tile.SUBTILE_HEIGHT, 0)
                    .rotate(Vector3.X,  60)
                    .rotate(Vector3.Z, -45);
                //batch.getProjectionMatrix()
                //    .translate(px + Tile.WIDTH50, py + Tile.HEIGHT - Tile.SUBTILE_HEIGHT, 0)
                //    .rotateRad(Vector3.Z, -0.463647609f)
                //    .shear;
                batch.begin();
                batch.setShader(null);
                BitmapFont font = Riiablo.fonts.consolas16;
                GlyphLayout layout = new GlyphLayout(font, desc);
                font.draw(batch, layout, 0, 0);
                /*GlyphLayout layout = new GlyphLayout(font, desc, 0, desc.length(), font.getColor(), 0, Align.center, false, null);
                font.draw(batch, layout,
                    px + Tile.WIDTH50,
                    py + Tile.HEIGHT - font.getLineHeight());
                */
                batch.end();
                batch.setProjectionMatrix(camera.combined);
                shapes.begin(ShapeRenderer.ShapeType.Filled);
              }
            }

            tx++;
            px += Tile.WIDTH50;
            py -= Tile.HEIGHT50;
          }

          startY2++;
          if (y >= tilesX - 1) {
            startX2++;
            startPy2 -= Tile.HEIGHT;
          } else {
            startX2--;
            startPx2 -= Tile.WIDTH;
          }
        }
        shapes.set(shapeType);

      default:
    }
  }

  private void drawDebugWalkable(ShapeRenderer shapes) {
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
      int tx = startX2;
      int ty = startY2;
      int px = startPx2;
      int py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(tx * Tile.SUBTILE_SIZE, ty * Tile.SUBTILE_SIZE);
        if (zone != null) {
          if (RENDER_DEBUG_WALKABLE == 1) {
            for (int sty = 0, t = 0; sty < Tile.SUBTILE_SIZE; sty++) {
              for (int stx = 0; stx < Tile.SUBTILE_SIZE; stx++, t++) {
                int flags = zone.flags[zone.getLocalTX(tx) * Tile.SUBTILE_SIZE + stx][zone.getLocalTY(ty) * Tile.SUBTILE_SIZE + sty] & 0xFF;
                if (flags == 0) continue;
                drawDebugWalkableTiles(shapes, px, py, t, flags);
              }
            }
          } else {
            //Map.Tile[][] tiles = zone.tiles[RENDER_DEBUG_WALKABLE - 1];
            //if (tiles != null) {
              Map.Tile tile = zone.get(RENDER_DEBUG_WALKABLE - 2, tx, ty);
              for (int t = 0; tile != null && tile.tile != null && t < Tile.NUM_SUBTILES; t++) {
                int flags = tile.tile.flags[WALKABLE_ID[t]] & 0xFF;
                if (flags == 0) continue;
                drawDebugWalkableTiles(shapes, px, py, t, flags);
              }
            //}
          }
        }

        tx++;
        px += Tile.WIDTH50;
        py -= Tile.HEIGHT50;
      }

      startY2++;
      if (y >= tilesX - 1) {
        startX2++;
        startPy2 -= Tile.HEIGHT;
      } else {
        startX2--;
        startPx2 -= Tile.WIDTH;
      }
    }

    shapes.set(shapeType);
  }

  private void drawDebugWalkableTiles(ShapeRenderer shapes, int px, int py, int t, int flags) {
    int offX = px + Tile.SUBTILE_OFFSET[t][0];
    int offY = py + Tile.SUBTILE_OFFSET[t][1];

    shapes.setColor(Color.CORAL);
    DebugUtils.drawDiamond2(shapes, offX, offY, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);

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

  private void drawDebugSpecial(ShapeRenderer shapes) {
    for (int i = Map.WALL_OFFSET, x, y; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      int startX2 = startX;
      int startY2 = startY;
      int startPx2 = startPx;
      int startPy2 = startPy;
      for (y = 0; y < viewBuffer.length; y++) {
        int tx = startX2;
        int ty = startY2;
        int stx = tx * Tile.SUBTILE_SIZE;
        int sty = ty * Tile.SUBTILE_SIZE;
        int px = startPx2;
        int py = startPy2;
        int size = viewBuffer[y];
        for (x = 0; x < size; x++) {
          Map.Zone zone = map.getZone(stx, sty);
          if (zone != null) {
            Map.Tile tile = zone.get(i, tx, ty);
            if (tile != null && Orientation.isSpecial(tile.cell.orientation)) {
              if (Map.ID.POPPADS.contains(tile.cell.id)) {
                shapes.setColor(Map.ID.getColor(tile.cell));
                Map.Preset preset = zone.getGrid(tx, ty);
                Map.Preset.PopPad popPad = preset.popPads.get(tile.cell.id);
                if (popPad.startX == zone.getGridX(tx) && popPad.startY == zone.getGridY(ty)) {
                  int width  = popPad.endX - popPad.startX;
                  int height = popPad.endY - popPad.startY;
                  shapes.line(
                      px + Tile.WIDTH50, py + Tile.HEIGHT,
                      px + Tile.WIDTH50 + (width  * Tile.SUBTILE_WIDTH50),
                      py + Tile.HEIGHT  - (height * Tile.SUBTILE_HEIGHT50));
                  shapes.line(
                      px + Tile.WIDTH50, py + Tile.HEIGHT,
                      px + Tile.WIDTH50 - (height * Tile.SUBTILE_WIDTH50),
                      py + Tile.HEIGHT  - (height * Tile.SUBTILE_HEIGHT50));
                  shapes.line(
                      px + Tile.WIDTH50 + (width * Tile.SUBTILE_WIDTH50),
                      py + Tile.HEIGHT - (height * Tile.SUBTILE_HEIGHT50),
                      px + Tile.WIDTH50 + (width * Tile.SUBTILE_WIDTH50) - (height * Tile.SUBTILE_WIDTH50),
                      py + Tile.HEIGHT - (height * Tile.SUBTILE_HEIGHT50) - (height * Tile.SUBTILE_HEIGHT50));
                  shapes.line(
                      px + Tile.WIDTH50 - (height * Tile.SUBTILE_WIDTH50),
                      py + Tile.HEIGHT - (height * Tile.SUBTILE_HEIGHT50),
                      px + Tile.WIDTH50 - (height * Tile.SUBTILE_WIDTH50) + (width  * Tile.SUBTILE_WIDTH50),
                      py + Tile.HEIGHT - (height * Tile.SUBTILE_HEIGHT50) - (height * Tile.SUBTILE_HEIGHT50));
                }
              } else {
                shapes.setColor(Color.WHITE);
                DebugUtils.drawDiamond2(shapes, px, py, Tile.WIDTH, Tile.HEIGHT);
              }
              shapes.end();

              batch.begin();
              batch.setShader(null);
              BitmapFont font = Riiablo.fonts.consolas12;
              String str = String.format(String.format("%s%n%08x", Map.ID.getName(tile.cell.id), tile.cell.value));
              GlyphLayout layout = new GlyphLayout(font, str, 0, str.length(), font.getColor(), 0, Align.center, false, null);
              font.draw(batch, layout,
                  px + Tile.WIDTH50,
                  py + Tile.HEIGHT50 + font.getLineHeight() / 4);
              batch.end();
              batch.setShader(Riiablo.shader);

              shapes.begin(ShapeRenderer.ShapeType.Line);
            }
          }

          tx++;
          stx += Tile.SUBTILE_SIZE;
          px += Tile.WIDTH50;
          py -= Tile.HEIGHT50;
        }

        startY2++;
        if (y >= tilesX - 1) {
          startX2++;
          startPy2 -= Tile.HEIGHT;
        } else {
          startX2--;
          startPx2 -= Tile.WIDTH;
        }
      }
    }
  }

  private void drawDebugObjects(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    shapes.set(ShapeRenderer.ShapeType.Filled);
    int startX2 = startX;
    int startY2 = startY;
    int x, y;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) {
          for (Entity entity : zone.entities) {
            Vector2 position = entity.position();
            if ((stx <= position.x && position.x < stx + Tile.SUBTILE_SIZE)
             && (sty <= position.y && position.y < sty + Tile.SUBTILE_SIZE)) {
              entity.drawDebug(batch, shapes);
            }
          }
        }
        for (Entity entity : entities) {
          Vector2 position = entity.position();
          if ((stx <= position.x && position.x < stx + Tile.SUBTILE_SIZE)
           && (sty <= position.y && position.y < sty + Tile.SUBTILE_SIZE)) {
            entity.drawDebug(batch, shapes);
          }
        }

        tx++;
        stx += Tile.SUBTILE_SIZE;
      }

      startY2++;
      if (y >= tilesX - 1) {
        startX2++;
      } else {
        startX2--;
      }
    }

    shapes.set(ShapeRenderer.ShapeType.Line);
  }

  private void drawDebugPaths(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    shapes.set(ShapeRenderer.ShapeType.Filled);
    int startX2 = startX;
    int startY2 = startY;
    int x, y;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) {
          for (Entity entity : zone.entities) {
            Vector2 position = entity.position();
            if ((stx <= position.x && position.x < stx + Tile.SUBTILE_SIZE)
             && (sty <= position.y && position.y < sty + Tile.SUBTILE_SIZE)) {
              entity.drawDebugPath(batch, shapes);
            }
          }
        }

        tx++;
        stx += Tile.SUBTILE_SIZE;
      }

      startY2++;
      if (y >= tilesX - 1) {
        startX2++;
      } else {
        startX2--;
      }
    }

    shapes.set(ShapeRenderer.ShapeType.Line);
  }

  public void drawDebugPath(ShapeRenderer shapes, MapGraph.MapGraphPath path) {
    drawDebugPath(shapes, path, Color.RED);
  }

  public void drawDebugPath(ShapeRenderer shapes, MapGraph.MapGraphPath path, Color color) {
    if (path == null || path.getCount() < 2) return;
    shapes.setProjectionMatrix(camera.combined);
    shapes.setColor(color);
    shapes.set(ShapeRenderer.ShapeType.Line);
    Iterator<MapGraph.Point2> it = new Array.ArrayIterator<>(path.nodes);
    MapGraph.Point2 src = it.next();
    for (MapGraph.Point2 dst; it.hasNext(); src = dst) {
      dst = it.next();
      EngineUtils.worldToScreenCoords(src.x, src.y, tmpVec2a);
      EngineUtils.worldToScreenCoords(dst.x, dst.y, tmpVec2b);
      shapes.line(tmpVec2a.x, tmpVec2a.y, tmpVec2b.x, tmpVec2b.y);
    }
  }
}
