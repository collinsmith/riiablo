package com.riiablo.map;

import java.util.Arrays;
import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;

import com.artemis.Aspect;
import com.artemis.BaseEntitySystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.utils.IntBag;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.Pools;

import com.riiablo.Riiablo;
import com.riiablo.camera.IsometricCamera;
import com.riiablo.codec.Animation;
import com.riiablo.codec.util.BBox;
import com.riiablo.engine.Engine;
import com.riiablo.engine.client.component.AnimationWrapper;
import com.riiablo.engine.client.component.BBoxWrapper;
import com.riiablo.engine.client.component.Hovered;
import com.riiablo.engine.client.component.Selectable;
import com.riiablo.engine.server.component.AIWrapper;
import com.riiablo.engine.server.component.Angle;
import com.riiablo.engine.server.component.AnimData;
import com.riiablo.engine.server.component.Class;
import com.riiablo.engine.server.component.Classname;
import com.riiablo.engine.server.component.CofReference;
import com.riiablo.engine.server.component.Networked;
import com.riiablo.engine.server.component.Object;
import com.riiablo.engine.server.component.Position;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DT1.Tile;
import com.riiablo.profiler.GpuSystem;
import com.riiablo.util.DebugUtils;

@GpuSystem
@All(AnimationWrapper.class)
public class RenderSystem extends BaseEntitySystem {
  private static final String TAG = "RenderSystem";
  private static final boolean DEBUG          = true;
  private static final boolean DEBUG_MATH     = DEBUG && !true;
  private static final boolean DEBUG_BUFFER   = DEBUG && true;
  private static final boolean DEBUG_SUBTILE  = DEBUG && !true;
  private static final boolean DEBUG_TILE     = DEBUG && !true;
  private static final boolean DEBUG_CAMERA   = DEBUG && true;
  private static final boolean DEBUG_OVERSCAN = DEBUG && true;
  private static final boolean DEBUG_GRID     = DEBUG && true;
  private static final boolean DEBUG_WALKABLE = DEBUG && !true;
  private static final boolean DEBUG_MATERIAL = DEBUG && !true;
  private static final boolean DEBUG_SPECIAL  = DEBUG && true;
  private static final boolean DEBUG_MOUSE    = DEBUG && true;
  private static final boolean DEBUG_POPPADS  = DEBUG && !true;
  private static final boolean DEBUG_ENTITIES = DEBUG && true;
  private static final boolean DEBUG_SELECT   = DEBUG && true;
  private static final boolean DEBUG_CELLS    = DEBUG && !true;

  public static boolean RENDER_DEBUG_SUBTILE  = DEBUG_SUBTILE;
  public static boolean RENDER_DEBUG_TILE     = DEBUG_TILE;
  public static boolean RENDER_DEBUG_CAMERA   = DEBUG_CAMERA;
  public static int     RENDER_DEBUG_OVERSCAN = DEBUG_OVERSCAN ? 0b010 : 0;
  public static int     RENDER_DEBUG_GRID     = DEBUG_GRID ? 3 : 0;
  public static int     RENDER_DEBUG_WALKABLE = DEBUG_WALKABLE ? 1 : 0;
  public static int     RENDER_DEBUG_MATERIAL = DEBUG_MATERIAL ? 1 : 0;
  public static boolean RENDER_DEBUG_SPECIAL  = DEBUG_SPECIAL;
  public static boolean RENDER_DEBUG_SELECT   = DEBUG_SELECT;
  public static int     RENDER_DEBUG_CELLS    = DEBUG_CELLS ? 1 : 0;
  public static boolean RENDER_DEBUG_ENTITIES = DEBUG_ENTITIES;

  private static final Color RENDER_DEBUG_GRID_COLOR_1 = new Color(0x3f3f3f3f);
  private static final Color RENDER_DEBUG_GRID_COLOR_2 = new Color(0x7f7f7f3f);
  private static final Color RENDER_DEBUG_GRID_COLOR_3 = new Color(0x0000ff3f);
  public static int DEBUG_GRID_MODES = 3;

  // Extra padding to ensure proper overscan, should be odd value
  private static final int TILES_PADDING_X = 3;
  private static final int TILES_PADDING_Y = 7;

  private final Comparator<Integer> SUBTILE_ORDER = new Comparator<Integer>() {
    @Override
    public int compare(Integer e1, Integer e2) {
      Vector2 pos1 = mPosition.get(e1).position;
      Vector2 pos2 = mPosition.get(e2).position;
      int i = Float.compare(pos1.y, pos2.y);
      return i == 0 ? Float.compare(pos1.x, pos2.x): i;
    }
  };

  protected ComponentMapper<AnimationWrapper> mAnimationWrapper;
  protected ComponentMapper<CofReference> mCofReference;
  protected ComponentMapper<Position> mPosition;
  protected ComponentMapper<Object> mObject;
//  private final Family family = Family.all(AnimationComponent.class, PositionComponent.class).get();
//  private ImmutableArray<Entity> entities;
//
//  // DEBUG
  protected ComponentMapper<Classname> mClassname;
  protected ComponentMapper<Class> mClass;
  protected ComponentMapper<Networked> mNetworked;
  protected ComponentMapper<BBoxWrapper> mBBoxWrapper;
  protected ComponentMapper<Angle> mAngle;
  protected ComponentMapper<Selectable> mSelectable;
  protected ComponentMapper<Hovered> mHovered;
  protected ComponentMapper<AIWrapper> mAIWrapper;
  protected ComponentMapper<AnimData> mAnimData;
  protected EntitySubscription debugEntitites;

  private final Vector2 tmpVec2 = new Vector2();

  PaletteIndexedBatch batch;
  IsometricCamera     iso;
  Map                 map;
  int                 viewBuffer[];
  Array<Integer>      cache[][][];
  int                 src = -1;
  boolean             dirty;
  final Vector2       currentPos = new Vector2();

  // sub-tile index in world-space
  int x, y;

  // sub-tile index in tile-space 2-D
  int stx, sty;

  // sub-tile index in tile-space 1-D
  int t;

  // pixel offset of sub-tile in world-space
  float spx, spy;

  // tile index in world-space
  int tx, ty;

  // pixel offset of tile in world-space
  float tpx, tpy;

  int width, height;
  int tilesX, tilesY;
  int renderWidth, renderHeight;

  // tile index of top right tile in render area
  int startX, startY;

  // tpx and tpy of startX, startY tile in world-space
  float startPx, startPy;

  // camera bounds
  int renderMinX, renderMinY;
  int renderMaxX, renderMaxY;

  float radius;

  // DT1 mainIndexes to not draw
  final Bits popped = new Bits();

  public RenderSystem(PaletteIndexedBatch batch, Map map) {
    this.batch = batch;
    this.map = map;
    this.iso = new IsometricCamera();
    iso.setToOrtho(false);
    iso.offset(0, -Tile.SUBTILE_HEIGHT50);
    iso.set(0, 0);
    iso.update();
    setClipPlane(-1000, 1000);
  }

  // This adjusts clip plane for debugging purposes (some elements rotated to map grid)
  private void setClipPlane(float near, float far) {
    iso.near = near;
    iso.far  = far;
    iso.update();
  }

  public IsometricCamera iso() {
    return iso;
  }

  public int getMinX() {
    return renderMinX;
  }

  public int getMinY() {
    return renderMinY;
  }

  public int getMaxX() {
    return renderMaxX;
  }

  public int getMaxY() {
    return renderMaxY;
  }

  @Override
  protected void initialize() {
    debugEntitites = world.getAspectSubscriptionManager().get(Aspect.all());
  }

  @Override
  protected void dispose() {
    debugEntitites = null;
  }

  public Map getMap() {
    return map;
  }

  public int getSrc() {
    return src;
  }

  public void setSrc(int src) {
    if (this.src != src) {
      assert mPosition.has(src) : "src entity must have a position component";
      this.src = src;
    }
  }

  public float radius() {
    return radius;
  }

  public boolean withinRadius(Vector2 vec) {
    return iso.position.dst(vec) <= radius;
  }

  public float zoom() {
    return iso.zoom;
  }

  public void zoom(float amt) {
    zoom(amt, false);
  }

  public void zoom(float amt, boolean resize) {
    if (iso.zoom != amt) {
      iso.zoom = amt;
      updatePosition(true);
      if (resize) resize();
    }
  }

  /**
   * resizes fov -- called rarely (typically only creation or screen resize)
   */
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
    dirty = true;

    cache = new Array[viewBufferLen][][];
    for (int i = 0; i < viewBufferLen; i++) {
      int viewBufferRun = viewBuffer[i];
      cache[i] = new Array[viewBufferRun][];
      for (int j = 0; j < viewBufferRun; j++) {
        cache[i][j] = new Array[] {
            new Array<Integer>(Tile.NUM_SUBTILES), // TODO: Really {@code (Tile.SUBTILE_SIZE - 1) * (Tile.SUBTILE_SIZE - 1)}
            new Array<Integer>(1), // better size TBD
            new Array<Integer>(Tile.SUBTILE_SIZE + Tile.SUBTILE_SIZE - 1), // only upper walls
        };
      }
    }
  }

  private void updateBounds() {
    width  = (int) (iso.viewportWidth  * iso.zoom);
    height = (int) (iso.viewportHeight * iso.zoom);

    updateCameraBounds();

    int minTilesX = ((Map.round(width)  + Tile.WIDTH  - 1) / Tile.WIDTH);
    int minTilesY = ((Map.round(height) + Tile.HEIGHT - 1) / Tile.HEIGHT);
    if ((minTilesX & 1) == 1) minTilesX++;
    if ((minTilesY & 1) == 1) minTilesY++;
    tilesX = minTilesX + TILES_PADDING_X;
    tilesY = minTilesY + TILES_PADDING_Y;
    renderWidth  = tilesX * Tile.WIDTH;
    renderHeight = tilesY * Tile.HEIGHT;
    assert (tilesX & 1) == 1;
    assert (tilesY & 1) == 1;

    float yardsX = (renderWidth  / 2) / Tile.SUBTILE_WIDTH;
    float yardsY = (renderHeight / 2) / Tile.SUBTILE_HEIGHT;
    radius = Vector2.len(yardsX, yardsY);
  }

  private void updateCameraBounds() {
    iso.toScreen(tmpVec2.set(iso.position));
    renderMinX = (int) tmpVec2.x - (width  >>> 1);
    renderMinY = (int) tmpVec2.y - (height >>> 1);
    renderMaxX = renderMinX + width;
    renderMaxY = renderMinY + height;
  }

  public void updatePosition() {
    updatePosition(false);
  }

  /**
   * updates position of camera -- once per frame
   */
  public void updatePosition(boolean force) {
    if (src == -1) return;
    Vector2 pos = mPosition.get(src).position;
    iso.set(pos);
    iso.update();
    if (pos.epsilonEquals(currentPos) && !force && !dirty) return;
    dirty = false;
    currentPos.set(pos);
    iso.toTile(tmpVec2.set(pos));
    this.x = (int) tmpVec2.x;
    this.y = (int) tmpVec2.y;

    // subtile index in tile-space
    stx = x < 0
        ? (x + 1) % Tile.SUBTILE_SIZE + (Tile.SUBTILE_SIZE - 1)
        : x % Tile.SUBTILE_SIZE;
    sty = y < 0
        ? (y + 1) % Tile.SUBTILE_SIZE + (Tile.SUBTILE_SIZE - 1)
        : y % Tile.SUBTILE_SIZE;
    t   = Tile.SUBTILE_INDEX[stx][sty];

    // pixel offset of subtile in world-space
    iso.toScreen(x, y, tmpVec2)
        .add(-Tile.SUBTILE_WIDTH50, -Tile.SUBTILE_HEIGHT50);
    spx = tmpVec2.x;
    spy = tmpVec2.y;

    // tile index in world-space
    tx = x < 0
        ? ((x + 1) / Tile.SUBTILE_SIZE) - 1
        : (x / Tile.SUBTILE_SIZE);
    ty = y < 0
        ? ((y + 1) / Tile.SUBTILE_SIZE) - 1
        : (y / Tile.SUBTILE_SIZE);

    // offset
    tpx = spx - Tile.SUBTILE_OFFSET[t][0];
    tpy = spy - Tile.SUBTILE_OFFSET[t][1];

    updateCameraBounds();

    final int offX = tilesX >>> 1;
    final int offY = tilesY >>> 1;
    startX = tx + offX - offY;
    startY = ty - offX - offY;
    startPx = tpx + renderWidth  / 2 - Tile.WIDTH50;
    startPy = tpy + renderHeight / 2 - Tile.HEIGHT50;

    if (DEBUG_MATH) {
      Gdx.app.debug(TAG,
          String.format("(%2d,%2d){%d,%d}[%2d,%2d](%dx%d)[%dx%d] %.0f,%.0f {%d,%d}->{%d,%d}",
              x, y, stx, sty, tx, ty, width, height, tilesX, tilesY, spx, spy, renderMinX, renderMinY, renderMaxX, renderMaxY));
    }

    updatePopPads();
  }

  private void updatePopPads() {
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

  @Override
  protected void begin() {
    Riiablo.batch.begin();
  }

  @Override
  protected void end() {
    Riiablo.batch.end();
  }

  /**
   * renders map
   */
  @Override
  protected void processSystem() {
    updatePosition();
    draw(world.delta);
  }

  /**
   * renders map
   */
  public void draw(float delta) {
    prepareBatch();
    buildCaches();
    drawBackground();
    drawMiddleground();
    drawForeground();
  }

  private void prepareBatch() {
    batch.setPalette(getPalette());
    batch.setProjectionMatrix(iso.combined);
  }

  private Texture getPalette() {
    switch (map.getAct()) {
      case 0:  return Riiablo.palettes.act1;
      case 1:  return Riiablo.palettes.act2;
      case 2:  return Riiablo.palettes.act3;
      case 3:  return Riiablo.palettes.act4;
      case 4:  return Riiablo.palettes.act5;
      default: return Riiablo.palettes.act1;
    }
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
  private void buildCaches() {
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
  }

  private void buildCache(Array<Integer>[] cache, Map.Zone zone, int stx, int sty) {
    cache[0].size = cache[1].size = cache[2].size = 0;
    int orderFlag;
    IntBag entitites = getEntityIds();
    for (int i = 0, size = entitites.size(); i < size; i++) {
      int id = entitites.get(i);
      Vector2 pos = mPosition.get(id).position;
      if ((stx <= pos.x && pos.x < stx + Tile.SUBTILE_SIZE)
       && (sty <= pos.y && pos.y < sty + Tile.SUBTILE_SIZE)) {
        Object objectComponent = mObject.get(id);
        if (objectComponent != null) {
          CofReference reference = mCofReference.get(id);
          orderFlag = objectComponent.base.OrderFlag[reference.mode];
        } else {
          orderFlag = stx == pos.x || sty == pos.y ? 2 : 0;
        }

        cache[orderFlag].add(id);
      }
    }
    cache[0].sort(SUBTILE_ORDER);
    cache[1].sort(SUBTILE_ORDER);
    cache[2].sort(SUBTILE_ORDER);
  }

  private void drawBackground() {
    int x, y;
    int startX2 = startX;
    int startY2 = startY;
    float startPx2 = startPx;
    float startPy2 = startPy;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      float px = startPx2;
      float py = startPy2;
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
      float px = startPx2;
      float py = startPy2;
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

  private void drawMiddleground() {
    int x, y;
    int startX2 = startX;
    int startY2 = startY;
    float startPx2 = startPx;
    float startPy2 = startPy;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      float px = startPx2;
      float py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) {
          //buildCaches(zone, stx, sty);
          Array<Integer>[] cache = this.cache[y][x];
          drawEntities(cache, 1); // floors
          drawEntities(cache, 2); // walls/doors
          drawWalls(batch, zone, tx, ty, px, py);
          //drawWalls (trees and maybe columns?)
          drawEntities(cache, 0); // objects
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

  private void drawForeground() {
    int x, y;
    int startX2 = startX;
    int startY2 = startY;
    float startPx2 = startPx;
    float startPy2 = startPy;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      float px = startPx2;
      float py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) {
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

  void drawEntities(Array<Integer>[] cache, int i) {
    for (int entity : cache[i]) {
//      if (!entity.target().isZero() && !entity.position().epsilonEquals(entity.target())) {
//        entity.angle(angle(entity.position(), entity.target()));
//      }

//      CofComponent cofComponent = this.cofComponent.get(entity);
//      if (cofComponent != null && cofComponent.load != Dirty.NONE) return;
      Animation animation = mAnimationWrapper.get(entity).animation;
      Vector2 pos = mPosition.get(entity).position;
      Vector2 tmp = iso.toScreen(tmpVec2.set(pos));
      animation.draw(batch, tmp.x, tmp.y);
    }
  }

  void drawLowerWalls(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, float px, float py) {
    if (px > renderMaxX || py > renderMaxY || px + Tile.WIDTH < renderMinX) return;
    for (int i = Map.WALL_OFFSET; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      Tile tile = zone.get(i, tx, ty);
      if (tile == null) continue;
      switch (tile.orientation) {
        case Orientation.LOWER_LEFT_WALL:
        case Orientation.LOWER_RIGHT_WALL:
        case Orientation.LOWER_NORTH_CORNER_WALL:
        case Orientation.LOWER_SOUTH_CORNER_WALL:
          batch.draw(tile.texture, px, py + tile.height + Tile.WALL_HEIGHT);
          // fall-through to continue
        default:
      }
    }
  }

  void drawFloors(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, float px, float py) {
    if (px > renderMaxX || py > renderMaxY) return;
    if (px + Tile.WIDTH < renderMinX || py + Tile.HEIGHT < renderMinY) return;
    for (int i = Map.FLOOR_OFFSET; i < Map.FLOOR_OFFSET + Map.MAX_FLOORS; i++) {
      Tile tile = zone.get(i, tx, ty);
      if (tile == null) continue;
      TextureRegion texture;
      int subst = map.warpSubsts.get(tile.id, -1);
      if (subst != -1) { // TODO: Performance can be improved if the reference is updated to below subst
        texture = map.dt1s.get(zone.level.LevelType).get(subst).texture;
      } else {
        texture = tile.texture;
      }
      //if (texture.getTexture().getTextureObjectHandle() == 0) return;
      batch.draw(texture, px, py);
    }
  }

  void drawShadows(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, float px, float py, Array<Integer>[] cache) {
    batch.setBlendMode(BlendMode.SOLID, Riiablo.colors.modal75);
    for (int i = Map.SHADOW_OFFSET; i < Map.SHADOW_OFFSET + Map.MAX_SHADOWS; i++) {
      Tile tile = zone.get(i, tx, ty);
      if (tile == null) continue;
      if (px > renderMaxX || px + Tile.WIDTH  < renderMinX) continue;
      TextureRegion texture = tile.texture;
      if (py > renderMaxY || py + texture.getRegionHeight() < renderMinY) continue;
      batch.draw(texture, px, py);
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
    for (Array<Integer> c : cache) {
      for (int entity : c) {
//        CofComponent cofComponent = this.cofComponent.get(entity);
//        if (cofComponent != null && cofComponent.load != Dirty.NONE) continue;
        Animation animation = mAnimationWrapper.get(entity).animation;
        Vector2 pos = mPosition.get(entity).position;
        Vector2 tmp = iso.toScreen(tmpVec2.set(pos));
        animation.drawShadow(batch, tmp.x, tmp.y, false);
      }
    }
    batch.resetBlendMode();
  }

  void drawWalls(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, float px, float py) {
    if (px > renderMaxX || py > renderMaxY || px + Tile.WIDTH < renderMinX) return;
    for (int i = Map.WALL_OFFSET; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      Tile tile = zone.get(i, tx, ty);
      if (tile == null || tile == null) continue;
      if (popped.get(tile.mainIndex)) continue;
      switch (tile.orientation) {
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
          if (py + tile.texture.getRegionHeight() < renderMinY) break;
          batch.draw(tile.texture, px, py);
          if (tile.orientation == Orientation.RIGHT_NORTH_CORNER_WALL) {
            Tile sibling = zone.dt1s.get(Orientation.LEFT_NORTH_CORNER_WALL, tile.mainIndex, tile.subIndex);
            batch.draw(sibling.texture, px, py);
          }
          // fall-through to continue
        default:
      }
    }
  }

  void drawRoofs(PaletteIndexedBatch batch, Map.Zone zone, int tx, int ty, float px, float py) {
    if (px > renderMaxX || px + Tile.WIDTH < renderMinX) return;
    for (int i = Map.WALL_OFFSET; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      Tile tile = zone.get(i, tx, ty);
      if (tile == null || tile == null) continue;
      if (popped.get(tile.mainIndex)) continue;
      if (!Orientation.isRoof(tile.orientation)) continue;
      if (py + tile.roofHeight > renderMaxY) continue;
      if (py + tile.roofHeight + tile.texture.getRegionHeight() < renderMinY) continue;
      batch.draw(tile.texture, px, py + tile.roofHeight);
    }
  }

  public void drawDebug(ShapeRenderer shapes) {
    batch.setProjectionMatrix(iso.combined);
    shapes.setProjectionMatrix(iso.combined);
    if (RENDER_DEBUG_MATERIAL > 0)
      drawDebugMaterial(shapes);

    if (RENDER_DEBUG_GRID > 0)
      drawDebugGrid(shapes);

    if (RENDER_DEBUG_WALKABLE > 0)
      drawDebugWalkable(shapes);

    if (RENDER_DEBUG_CELLS > 0)
      drawDebugCells(shapes);

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

    if (RENDER_DEBUG_ENTITIES)
      drawDebugObjects(shapes);

    if (RENDER_DEBUG_CAMERA)
      drawDebugCamera(shapes);

    if (RENDER_DEBUG_OVERSCAN > 0)
      drawDebugOverscan(shapes);

    if (DEBUG_MOUSE)
      drawDebugMouse(shapes);
  }

  private void drawDebugGrid(ShapeRenderer shapes) {
    int x, y;
    switch (RENDER_DEBUG_GRID) {
      case 1:
        shapes.setColor(RENDER_DEBUG_GRID_COLOR_1);
        float startPx2 = startPx;
        float startPy2 = startPy;
        for (y = 0; y < viewBuffer.length; y++) {
          float px = startPx2;
          float py = startPy2;
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
          float px = startPx2;
          float py = startPy2;
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
          float px = startPx2;
          float py = startPy2;
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
                batch.setProjectionMatrix(iso.combined);
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
         5,  6,  7,  8,  9,
         0,  1,  2,  3,  4
    };

    ShapeRenderer.ShapeType shapeType = shapes.getCurrentType();
    shapes.set(ShapeRenderer.ShapeType.Filled);

    int startX2 = startX;
    int startY2 = startY;
    float startPx2 = startPx;
    float startPy2 = startPy;
    int x, y;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      float px = startPx2;
      float py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(tx * Tile.SUBTILE_SIZE, ty * Tile.SUBTILE_SIZE);
        if (zone != null) {
          if (RENDER_DEBUG_WALKABLE == 1) {
            for (int sty = 0, t = 0; sty < Tile.SUBTILE_SIZE; sty++) {
              for (int stx = 0; stx < Tile.SUBTILE_SIZE; stx++, t++) {
                int flags = zone.flags(zone.getLocalTX(tx) * Tile.SUBTILE_SIZE + stx, zone.getLocalTY(ty) * Tile.SUBTILE_SIZE + sty);
                if (flags == 0) continue;
                drawDebugWalkableTiles(shapes, px, py, t, flags);
              }
            }
          } else {
            //Map.Tile[][] tiles = zone.tiles[RENDER_DEBUG_WALKABLE - 1];
            //if (tiles != null) {
              Tile tile = zone.get(RENDER_DEBUG_WALKABLE - 2, tx, ty);
              for (int t = 0; tile != null && tile != null && t < Tile.NUM_SUBTILES; t++) {
                int flags = tile.flags[WALKABLE_ID[t]] & 0xFF;
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

  private static void drawDebugWalkableTiles(ShapeRenderer shapes, float px, float py, int t, int flags) {
    float offX = px + Tile.SUBTILE_OFFSET[t][0];
    float offY = py + Tile.SUBTILE_OFFSET[t][1];

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

  private void drawDebugMaterial(ShapeRenderer shapes) {
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
    float startPx2 = startPx;
    float startPy2 = startPy;
    int x, y;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      float px = startPx2;
      float py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(tx * Tile.SUBTILE_SIZE, ty * Tile.SUBTILE_SIZE);
        if (zone != null) {
          if (RENDER_DEBUG_MATERIAL == 1) {
            for (int sty = 0, t = 0; sty < Tile.SUBTILE_SIZE; sty++) {
              for (int stx = 0; stx < Tile.SUBTILE_SIZE; stx++, t++) {
                int flags = zone.flags(zone.getLocalTX(tx) * Tile.SUBTILE_SIZE + stx, zone.getLocalTY(ty) * Tile.SUBTILE_SIZE + sty);
                if (flags != 0) continue;
                // TODO: zone.type(x, y) which is an xor of type for the layers
              }
            }
          } else {
            Tile tile = zone.get(RENDER_DEBUG_MATERIAL - 2, tx, ty);
            for (int t = 0; tile != null && tile != null && t < Tile.NUM_SUBTILES; t++) {
              int flags = tile.flags[WALKABLE_ID[t]] & 0xFF;
              if (flags != 0) continue;
              DT1Materials.Material type = DT1Materials.getMaterial(zone.level, tile);
              drawDebugMaterialTiles(shapes, px, py, t, type);
            }
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

  private static void drawDebugMaterialTiles(ShapeRenderer shapes, float px, float py, int t, DT1Materials.Material type) {
    float offX = px + Tile.SUBTILE_OFFSET[t][0];
    float offY = py + Tile.SUBTILE_OFFSET[t][1];

    shapes.setColor(type.color);
    shapes.set(ShapeRenderer.ShapeType.Filled);
    DebugUtils.drawDiamond2(shapes, offX, offY, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
    shapes.set(ShapeRenderer.ShapeType.Filled);
  }

  private void drawDebugCells(ShapeRenderer shapes) {
    int type;
    int l = RENDER_DEBUG_CELLS - 1;
    if (Map.FLOOR_OFFSET <= l && l < Map.FLOOR_OFFSET + Map.MAX_FLOORS) {
      l -= Map.FLOOR_OFFSET;
      type = 0;
    } else if (Map.SHADOW_OFFSET <= l && l < Map.SHADOW_OFFSET + Map.MAX_SHADOWS) {
      l -= Map.SHADOW_OFFSET;
      type = 1;
    } else if (Map.WALL_OFFSET <= l && l < Map.WALL_OFFSET + Map.MAX_WALLS) {
      l -= Map.WALL_OFFSET;
      type = 2;
    } else if (Map.TAG_OFFSET <= l && l < Map.TAG_OFFSET + Map.MAX_TAGS) {
      l -= Map.TAG_OFFSET;
      type = 3;
    } else {
      return;
    }

    shapes.end();
    batch.begin();
    batch.setShader(null);
    int startX2 = startX;
    int startY2 = startY;
    float startPx2 = startPx;
    float startPy2 = startPy;
    int x, y;
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      float px = startPx2;
      float py = startPy2;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        Map.Zone zone = map.getZone(stx, sty);
        if (zone != null) {
          Map.Preset preset = zone.getGrid(tx, ty);
          if (preset != null) {
            DS1 ds1 = preset.ds1;
            int value = 0;
            int gx = (tx - zone.tx) % zone.gridSizeX;
            int gy = (ty - zone.ty) % zone.gridSizeY;
            switch (type) {
              case 0: {
                if (l >= ds1.numFloors) break;
                int ptr = l + (gy * ds1.floorLine) + (gx * ds1.numFloors);
                value = ds1.floors[ptr].value;
              }
                break;
              case 1: {
                if (l >= ds1.numShadows) break;
                int ptr = l + (gy * ds1.shadowLine) + (gx * ds1.numShadows);
                value = ds1.shadows[ptr].value;
              }
                break;
              case 2: {
                if (l >= ds1.numWalls) break;
                int ptr = l + (gy * ds1.wallLine) + (gx * ds1.numWalls);
                value = ds1.walls[ptr].value;
              }
                break;
              case 3: {
                if (l >= ds1.numTags) break;
                int ptr = l + (gy * ds1.tagLine) + (gx * ds1.numTags);
                value = ds1.tags[ptr];
              }
                break;
              default:
                value = 0;
            }

            if (value != 0) {
              value &= ~(DS1.Cell.MAIN_INDEX_MASK | DS1.Cell.SUB_INDEX_MASK);
              BitmapFont font = Riiablo.fonts.consolas12;
              String str = String.format("%08x", value);
              GlyphLayout layout = new GlyphLayout(font, str, 0, str.length(), font.getColor(), 0, Align.center, false, null);
              font.draw(batch, layout,
                  px + Tile.WIDTH50,
                  py + Tile.HEIGHT50 + font.getLineHeight() / 4);
            }
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

    batch.end();
    batch.setShader(Riiablo.shader);
    shapes.begin(ShapeRenderer.ShapeType.Line);
  }

  private void drawDebugSpecial(ShapeRenderer shapes) {
    for (int i = Map.WALL_OFFSET, x, y; i < Map.WALL_OFFSET + Map.MAX_WALLS; i++) {
      int startX2 = startX;
      int startY2 = startY;
      float startPx2 = startPx;
      float startPy2 = startPy;
      for (y = 0; y < viewBuffer.length; y++) {
        int tx = startX2;
        int ty = startY2;
        int stx = tx * Tile.SUBTILE_SIZE;
        int sty = ty * Tile.SUBTILE_SIZE;
        float px = startPx2;
        float py = startPy2;
        int size = viewBuffer[y];
        for (x = 0; x < size; x++) {
          Map.Zone zone = map.getZone(stx, sty);
          if (zone != null) {
            DS1.Cell cell = zone.getCell(i, tx, ty);
            if (cell != null) {
              if (Map.ID.POPPADS.contains(cell.id)) {
                shapes.setColor(Map.ID.getColor(cell));
                Map.Preset preset = zone.getGrid(tx, ty);
                Map.Preset.PopPad popPad = preset.popPads.get(cell.id);
                if (popPad.startX == zone.getGridX(tx) && popPad.startY == zone.getGridY(ty)) {
                  int width  = popPad.endX - popPad.startX;
                  int height = popPad.endY - popPad.startY;
                  iso.getPixOffset(tmpVec2);
                  float offsetX = tmpVec2.x;
                  float offsetY = tmpVec2.y;
                  iso.toScreen(tmpVec2.set(stx, sty));
                  float topLeftX = tmpVec2.x - offsetX;
                  float topLeftY = tmpVec2.y - offsetY;
                  iso.toScreen(tmpVec2.set(stx, sty).add(width, 0));
                  float topRightX = tmpVec2.x - offsetX;
                  float topRightY = tmpVec2.y - offsetY;
                  iso.toScreen(tmpVec2.set(stx, sty).add(0, height));
                  float bottomLeftX = tmpVec2.x - offsetX;
                  float bottomLeftY = tmpVec2.y - offsetY;
                  iso.toScreen(tmpVec2.set(stx, sty).add(width, height));
                  float bottomRightX = tmpVec2.x - offsetX;
                  float bottomRightY = tmpVec2.y - offsetY;
                  shapes.line(topLeftX, topLeftY, topRightX, topRightY);
                  shapes.line(topRightX, topRightY, bottomRightX, bottomRightY);
                  shapes.line(bottomRightX, bottomRightY, bottomLeftX, bottomLeftY);
                  shapes.line(bottomLeftX, bottomLeftY, topLeftX, topLeftY);
                }
              } else {
                shapes.setColor(Color.WHITE);
                DebugUtils.drawDiamond2(shapes, px, py, Tile.WIDTH, Tile.HEIGHT);
              }
              shapes.end();

              batch.begin();
              batch.setShader(null);
              BitmapFont font = Riiablo.fonts.consolas12;
              String str = String.format("%s%n%08x", Map.ID.getName(cell.id), cell.value);
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

  private void drawDebugObjects(ShapeRenderer shapes) {
    shapes.set(ShapeRenderer.ShapeType.Line);
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
        IntBag entities = debugEntitites.getEntities();
        for (int i = 0, numEntities = entities.size(); i < numEntities; i++) {
          int id = entities.get(i);
          Vector2 position = mPosition.get(id).position;
          if ((stx <= position.x && position.x < stx + Tile.SUBTILE_SIZE)
           && (sty <= position.y && position.y < sty + Tile.SUBTILE_SIZE)) {
            Vector2 tmp = iso.agg(tmpVec2.set(position)).toScreen().ret();
            shapes.setColor(Color.WHITE);
            DebugUtils.drawDiamond(shapes, tmp.x, tmp.y, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
            if (RENDER_DEBUG_SELECT && mSelectable.has(id)) {
              BBoxWrapper boxWrapper = mBBoxWrapper.get(id);
              if (boxWrapper != null) {
                BBox box = boxWrapper.box;
                if (box != null) {
                  shapes.setColor(mHovered.has(id) ? Color.GREEN : Color.GRAY);
                  shapes.rect(tmpVec2.x + box.xMin, tmpVec2.y - box.yMax, box.width, box.height);
                }
              }
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

    shapes.end();
    batch.begin();
    batch.setShader(null);
    startX2 = startX;
    startY2 = startY;
    StringBuilder builder = new StringBuilder(64);
    for (y = 0; y < viewBuffer.length; y++) {
      int tx = startX2;
      int ty = startY2;
      int stx = tx * Tile.SUBTILE_SIZE;
      int sty = ty * Tile.SUBTILE_SIZE;
      int size = viewBuffer[y];
      for (x = 0; x < size; x++) {
        IntBag entities = debugEntitites.getEntities();
        for (int i = 0, numEntities = entities.size(); i < numEntities; i++) {
          int id = entities.get(i);
          Vector2 position = mPosition.get(id).position;
          if ((stx <= position.x && position.x < stx + Tile.SUBTILE_SIZE)
           && (sty <= position.y && position.y < sty + Tile.SUBTILE_SIZE)) {
            builder.setLength(0);

            builder.append(id);
            Networked networked = mNetworked.get(id);
            if (networked != null) {
              builder
                  .append(' ')
                  .append('(')
                  .append(networked.serverId)
                  .append(')');
            }
            builder.append('\n');

            builder.append(mClassname.get(id).classname).append('\n');

            Class.Type type = mClass.get(id).type;
            builder.append(type).append('\n');

            CofReference reference = mCofReference.get(id);
            if (reference != null) {
              builder
                  .append(reference.token.toUpperCase())
                  .append(' ')
                  .append(type.getMode(reference.mode))
                  .append(' ')
                  .append(Engine.getWClass(reference.wclass))
                  .append('\n');
            }
            Angle angle = mAngle.get(id);
            if (angle != null) {
              builder
                  .append(String.format("%.02f", angle.angle.angleRad()))
                  .append('\n');
            }
            AnimData animData = mAnimData.get(id);
            if (animData != null) {
              builder
                  .append(StringUtils.leftPad(String.format("%d.%02X", animData.frame >>> 8, animData.frame & 0xFF), animData.numFrames >>> 8 > 10 ? 5 : 4))
                  .append('/')
                  .append(Integer.toString(animData.numFrames >>> 8))
                  .append(' ')
                  .append(String.format("%02X", animData.override >= 0 ? animData.override : animData.speed))
                  .append('\n');
            }
            AnimationWrapper animationWrapper = mAnimationWrapper.get(id);
            if (animationWrapper != null) {
              Animation animation = animationWrapper.animation;
              if (animation.getNumFramesPerDir() > 1) {
                builder
                    .append(StringUtils.leftPad(Integer.toString(animation.getFrame()), 2))
                    .append('/')
                    .append(Integer.toString(animation.getNumFramesPerDir() - 1))
                    .append(' ')
                    .append(animation.getFrameDelta())
                    .append('\n');
              }
            }
            AIWrapper aiComponent = mAIWrapper.get(id);
            if (aiComponent != null) {
              builder.append(aiComponent.ai.getState()).append('\n');
            }
            Vector2 tmp = iso.agg(tmpVec2.set(position)).toScreen().ret();
            GlyphLayout layout = Riiablo.fonts.consolas12.draw(batch, builder.toString(), tmp.x, tmp.y - Tile.SUBTILE_HEIGHT50 - 4, 0, Align.center, false);
            Pools.free(layout);
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
    batch.end();
    batch.setShader(Riiablo.shader);
    shapes.begin();
  }

  private void drawDebugCamera(ShapeRenderer shapes) {
    Vector2 tmp = tmpVec2.set(this.currentPos);
    iso.toScreen(tmp);
    float viewportWidth  = width;
    float viewportHeight = height;
    shapes.setColor(Color.GREEN);
    shapes.rect(
        tmp.x - MathUtils.ceil(viewportWidth  / 2) - 1,
        tmp.y - MathUtils.ceil(viewportHeight / 2) - 1,
        viewportWidth + 2, viewportHeight + 2);
  }

  private void drawDebugOverscan(ShapeRenderer shapes) {
    if ((RENDER_DEBUG_OVERSCAN & 0b001) == 0b001) {
      shapes.setColor(Color.LIGHT_GRAY);
      shapes.rect(
          tpx - renderWidth  / 2 + Tile.WIDTH50,
          tpy - renderHeight / 2 + Tile.HEIGHT50 + renderHeight,
          renderWidth, 96);
    }
    if ((RENDER_DEBUG_OVERSCAN & 0b010) == 0b010) {
      shapes.setColor(Color.GRAY);
      shapes.rect(
          tpx - renderWidth / 2 + Tile.WIDTH50,
          tpy - renderHeight / 2 + Tile.HEIGHT50,
          renderWidth, renderHeight);
    }
    if ((RENDER_DEBUG_OVERSCAN & 0b100) == 0b100) {
      shapes.setColor(Color.DARK_GRAY);
      shapes.rect(
          tpx - renderWidth / 2 + Tile.WIDTH50,
          tpy - renderHeight / 2 + Tile.HEIGHT50 - 96,
          renderWidth, 96);
    }
  }

  private void drawDebugMouse(ShapeRenderer shapes) {
    Vector2 tmp = iso.agg(tmpVec2.set(Gdx.input.getX(), Gdx.input.getY())).unproject().toWorld().toTile().toScreen().ret();
    shapes.set(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(Color.SALMON);
    DebugUtils.drawDiamond(shapes, tmp.x, tmp.y, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
  }
}
