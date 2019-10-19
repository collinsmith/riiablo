package com.riiablo.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.Pools;
import com.riiablo.Riiablo;
import com.riiablo.codec.Animation;
import com.riiablo.codec.COF;
import com.riiablo.codec.COFD2;
import com.riiablo.codec.DC;
import com.riiablo.codec.DCC;
import com.riiablo.codec.excel.Overlay;
import com.riiablo.codec.excel.Skills;
import com.riiablo.codec.util.BBox;
import com.riiablo.graphics.BlendMode;
import com.riiablo.graphics.PaletteIndexedBatch;
import com.riiablo.map.DS1;
import com.riiablo.map.DT1.Tile;
import com.riiablo.map.Map;
import com.riiablo.map.MapGraph;
import com.riiablo.screen.GameScreen;
import com.riiablo.util.DebugUtils;
import com.riiablo.util.EngineUtils;
import com.riiablo.widget.Label;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public abstract class Entity implements Animation.AnimationListener {
  private static final String TAG = "Entity";

  private static final boolean DEBUG        = true;
  private static final boolean DEBUG_STATE  = DEBUG && !true;
  private static final boolean DEBUG_DIRTY  = DEBUG && !true;
  private static final boolean DEBUG_COF    = DEBUG && !true;
  private static final boolean DEBUG_TARGET = DEBUG && true;
  private static final boolean DEBUG_PATH   = DEBUG && !true;
  private static final boolean DEBUG_SIZE   = DEBUG && !true;
  private static final boolean DEBUG_STATUS = DEBUG && !true;
  private static final boolean DEBUG_LOAD   = DEBUG && !true;

  protected enum Type {
    OBJ("OBJECTS",
        new String[] {"NU", "OP", "ON", "S1", "S2", "S3", "S4", "S5"},
        new String[] {"NIL", "LIT"}),
    MON("MONSTERS",
        new String[] {
            "DT", "NU", "WL", "GH", "A1", "A2", "BL", "SC", "S1", "S2", "S3", "S4", "DD", "GH",
            "XX", "RN"
        },
        new String[] {
            "NIL", "LIT", "MED", "HEV", "HVY", "DES", "BRV", "AXE", "FLA", "HAX", "MAC", "SCM",
            "BUC", "LRG", "KIT", "SML", "LSD", "WND", "SSD", "CLB", "TCH", "BTX", "HAL", "LAX",
            "MAU", "SCY", "WHM", "WHP", "JAV", "OPL", "GPL", "SBW", "LBW", "LBB", "SBB", "PIK",
            "SPR", "TRI", "FLC", "SBR", "GLV", "PAX", "BSD", "FLB", "WAX", "WSC", "WSD", "CLM",
            "SMC", "FIR", "LHT", "CLD", "POS", "RSP", "LSP", "UNH", "RSG", "BLD", "SHR", "LHR",
            "HBD", "TKT", "BAB", "PHA", "FAN", "PON", "HD1", "HD2", "HD3", "HD4", "ZZ1", "ZZ2",
            "ZZ3", "ZZ4", "ZZ5", "ZZ6", "ZZ7", "RED", "TH2", "TH3", "TH4", "TH5", "FBL", "FSP",
            "YNG", "OLD", "BRD", "GOT", "FEZ", "ROL", "BSK", "BUK", "SAK", "BAN", "FSH", "SNK",
            "BRN", "BLK", "SRT", "LNG", "DLN", "BTP", "MTP", "STP", "SVT", "COL", "HOD", "HRN",
            "LNK", "TUR", "MLK", "FHM", "GHM", "BHN", "HED",
        }),
    PLR("CHARS",
        new String[] {
            "DT", "NU", "WL", "RN", "GH", "TN", "TW", "A1", "A2", "BL", "SC", "TH", "KK", "S1",
            "S2", "S3", "S4", "DD", "GH", "GH"
        },
        new String[] {
            "NIL", "LIT", "MED", "HVY", "HAX", "AXE", "LAX", "BTX", "GIX", "WND", "YWN", "BWN",
            "CLB", "MAC", "WHM", "FLA", "MAU", "SSD", "SCM", "FLC", "CRS", "BSD", "LSD", "CLM",
            "GSD", "DGR", "DIR", "JAV", "PIL", "GLV", "SPR", "TRI", "BRN", "PIK", "HAL", "SCY",
            "PAX", "BST", "SST", "CST", "LST", "SBW", "LBW", "CLW", "SKR", "KTR", "AXF", "SBB",
            "LBB", "LXB", "HXB", "OB1", "OB3", "OB4", "AM1", "AM2", "AM3", "CAP", "SKP", "HLM",
            "FHL", "GHM", "CRN", "MSK", "QLT", "LEA", "HLA", "STU", "RNG", "SCL", "CHN", "BRS",
            "SPL", "PLT", "FLD", "GTH", "FUL", "AAR", "LTP", "BUC", "LRG", "KIT", "TOW", "BHM",
            "BSH", "SPK", "DR1", "DR4", "DR3", "BA1", "BA3", "BA5", "PA1", "PA3", "PA5", "NE1",
            "NE2", "NE3", "_62", "_63", "_64", "_65", "_66", "_67", "_68", "_69", "_6A", "_6B",
            "_6C", "_6D", "_6E", "_6F", "_70", "_71", "_72", "_73", "_74", "_75", "_76", "_77",
            "_78", "_79", "_7A", "_7B", "_7C", "GPL", "OPL", "GPS", "OPS",
        }) {
          @Override
          COFD2 getCOFs() {
            return Riiablo.cofs.chars_cof;
          }
        },
    ITM("ITEMS",
        new String[] {"NU"},
        new String[] {"NIL"}),
    WRP("WARPS",
        new String[] {"NU"},
        new String[] {"NIL"}),
    MIS("MISSILES",
        new String[] {"NU"},
        new String[] {"NIL"});

    public final String PATH;
    public final String MODE[];
    public final String COMP[];

    private ObjectIntMap<String> MODES;
    private ObjectIntMap<String> COMPS;

    Type(String path, String[] modes, String[] comps) {
      PATH = "data\\global\\" + path;
      MODE = modes;
      MODES = new ObjectIntMap<>();
      for (int i = 0; i < modes.length; i++) MODES.put(modes[i].toLowerCase(), i);
      COMP = comps;
      COMPS = new ObjectIntMap<>();
      for (int i = 0; i < comps.length; i++) COMPS.put(comps[i].toLowerCase(), i);
    }

    COFD2 getCOFs() {
      return Riiablo.cofs.active;
    }

    public byte getMode(String mode) {
      return (byte) MODES.get(mode.toLowerCase(), -1);
    }

    public int getComponent(String comp) {
      return COMPS.get(comp.toLowerCase(), -1);
    }
  }

  private static final String[] WCLASS = {
      "", "HTH", "BOW", "1HS", "1HT", "STF", "2HS", "2HT", "XBW", "1JS", "1JT", "1SS", "1ST", "HT1", "HT2"
  };
  
  public static final byte WEAPON_NIL =  0;
  public static final byte WEAPON_HTH =  1;
  public static final byte WEAPON_BOW =  2;
  public static final byte WEAPON_1HS =  3;
  public static final byte WEAPON_1HT =  4;
  public static final byte WEAPON_STF =  5;
  public static final byte WEAPON_2HS =  6;
  public static final byte WEAPON_2HT =  7;
  public static final byte WEAPON_XBW =  8;
  public static final byte WEAPON_1JS =  9;
  public static final byte WEAPON_1JT = 10;
  public static final byte WEAPON_1SS = 11;
  public static final byte WEAPON_1ST = 12;
  public static final byte WEAPON_HT1 = 13;
  public static final byte WEAPON_HT2 = 14;

  private static final String[] COMPOSIT = {
      "HD", "TR", "LG", "RA", "LA", "RH", "LH", "SH", "S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8",
  };

  public static final class Dirty {
    public static final int NONE = 0;
    public static final int HD = 1 << 0;
    public static final int TR = 1 << 1;
    public static final int LG = 1 << 2;
    public static final int RA = 1 << 3;
    public static final int LA = 1 << 4;
    public static final int RH = 1 << 5;
    public static final int LH = 1 << 6;
    public static final int SH = 1 << 7;
    public static final int S1 = 1 << 8;
    public static final int S2 = 1 << 9;
    public static final int S3 = 1 << 10;
    public static final int S4 = 1 << 11;
    public static final int S5 = 1 << 12;
    public static final int S6 = 1 << 13;
    public static final int S7 = 1 << 14;
    public static final int S8 = 1 << 15;
    public static final int ALL = 0xFFFF;

    public static String toString(int bits) {
      StringBuilder builder = new StringBuilder();
      if (bits == NONE) {
        builder.append("NONE");
      } else if (bits == ALL) {
        builder.append("ALL");
      } else {
        if ((bits & HD) == HD) builder.append("HD").append("|");
        if ((bits & TR) == TR) builder.append("TR").append("|");
        if ((bits & LG) == LG) builder.append("LG").append("|");
        if ((bits & RA) == RA) builder.append("RA").append("|");
        if ((bits & LA) == LA) builder.append("LA").append("|");
        if ((bits & RH) == RH) builder.append("RH").append("|");
        if ((bits & LH) == LH) builder.append("LH").append("|");
        if ((bits & SH) == SH) builder.append("SH").append("|");
        if ((bits & S1) == S1) builder.append("S1").append("|");
        if ((bits & S2) == S2) builder.append("S2").append("|");
        if ((bits & S3) == S3) builder.append("S3").append("|");
        if ((bits & S4) == S4) builder.append("S4").append("|");
        if ((bits & S5) == S5) builder.append("S5").append("|");
        if ((bits & S6) == S6) builder.append("S6").append("|");
        if ((bits & S7) == S7) builder.append("S7").append("|");
        if ((bits & S8) == S8) builder.append("S8").append("|");
        if (builder.length() > 0) builder.setLength(builder.length() - 1);
      }
      return builder.toString();
    }

    public static boolean isDirty(int flags, int component) {
      return ((1 << component) & flags) != 0;
    }
  }

  private static final float DEFAULT_ANGLE = MathUtils.atan2(-1, -2); // Direction 0
  private static final byte[] DEFAULT_TRANS;
  static {
    DEFAULT_TRANS = new byte[COF.Component.NUM_COMPONENTS];
    Arrays.fill(DEFAULT_TRANS, (byte) 0xFF);
  }
  private static final float[] DEFAULT_ALPHA;
  static {
    DEFAULT_ALPHA = new float[COF.Component.NUM_COMPONENTS];
    Arrays.fill(DEFAULT_ALPHA, 1.0f);
  }

  private static final int[][][] SIZES = {
      { {0,0} },
      { {0,0}, {-1,0}, {1,0}, {0,-1}, {0,1} },
      { {0,0}, {-1,0}, {1,0}, {0,-1}, {0,1}, {-1,-1}, {1,1}, {1,-1}, {-1,1}, {-2,0}, {2,0}, {0,-2}, {0,2} },
  };

  int     uuid = 0;

  String  classname;
  Type    type;
  int     dirty;

  String  token;
  byte    code;
  byte    mode;
  byte    wclass;
  String  cof;
  byte    comp[];
  byte    trans[]; // TODO: Could also assign DEFAULT_TRANS and lazy change
  float   alpha[];
  float   angle = DEFAULT_ANGLE;
  Vector2 position = new Vector2();
  Vector2 screen = new Vector2();

  @SuppressWarnings("unchecked")
  final AssetDescriptor<? extends DC>[] layer = (AssetDescriptor<? extends DC>[]) new AssetDescriptor[COF.Component.NUM_COMPONENTS];
  int     load;

  Animation animation;

  String  name;
  Actor   label;
  boolean over;

  byte    nextMode = -1;

  Vector2 target = new Vector2();
  MapGraph.MapGraphPath path = new MapGraph.MapGraphPath();
  Iterator<MapGraph.Point2> targets = Collections.emptyIterator();

  boolean running = false;
  float   walkSpeed = 6;
  float   runSpeed  = 9;

  Overlay.Entry overlayEntry;
  Animation overlay;

  int     size = 1;

  private static final Vector2 tmpVec2 = new Vector2();

  public static Entity create(Map map, Map.Zone zone, DS1 ds1, DS1.Object object) {
    final int type = object.type;
    switch (type) {
      case DS1.Object.DYNAMIC_TYPE:
        return Monster.create(map, zone, ds1, object);
      case DS1.Object.STATIC_TYPE:
        return Object.create(map, zone, ds1, object);
      default:
        Gdx.app.error(TAG, "Unexpected type: " + type);
        return null;
    }
  }

  Entity(Type type, String classname, String token) {
    this(type, classname, token, new byte[COF.Component.NUM_COMPONENTS], DEFAULT_TRANS.clone());
  }

  Entity(Type type, String classname, String token, byte[] components, byte[] transforms) {
    this.type = type;
    this.classname = classname;
    this.token = token;
    mode = code = getNeutralMode();
    wclass = WEAPON_HTH;
    comp = components;
    trans = transforms;
    alpha = DEFAULT_ALPHA.clone();
    invalidate();

    // TODO: lazy init
    Label label = new Label(Riiablo.fonts.font16);
    label.setUserObject(this);
    label.setAlignment(Align.center);
    label.getStyle().background = Label.MODAL;
    this.label = label;
  }

  public void setMode(byte mode) {
    setMode(mode, mode);
  }

  public void setMode(byte mode, byte code) {
    if (this.mode != mode) {
      if (DEBUG_STATE) Gdx.app.debug(TAG, classname + " mode: " + type.MODE[this.mode] + " -> " + type.MODE[mode]);
      this.mode = mode;
      invalidate();
    }

    this.code = code;
  }

  public void setWeapon(byte wclass) {
    if (this.wclass != wclass) {
      if (DEBUG_STATE) Gdx.app.debug(TAG, classname + " wclass: " + WCLASS[this.wclass] + " -> " + WCLASS[wclass]);
      this.wclass = wclass;
      invalidate();
    }
  }

  public void setComponents(byte[] components) {
    System.arraycopy(components, 0, comp, 0, COF.Component.NUM_COMPONENTS);
    invalidate();
  }

  public void setComponent(byte component, byte code) {
    if (comp[component] != code) {
      if (DEBUG_STATE) Gdx.app.debug(TAG, classname + " component: " + type.COMP[comp[component]] + " -> " + type.COMP[code == -1 ? 0 : code]);
      comp[component] = code;
      invalidate(1 << component);
    }
  }

  public void setTransforms(byte[] transforms) {
    System.arraycopy(transforms, 0, trans, 0, COF.Component.NUM_COMPONENTS);
    invalidate(); // TODO: invalidate not necessary -- but this method isn't used anywhere performance critical
  }

  public void setTransform(byte component, byte transform) {
    if (trans[component] != transform) {
      if (DEBUG_STATE) Gdx.app.debug(TAG, classname + " transform: " + (trans[component] & 0xFF) + " -> " + (transform & 0xFF)); // TODO: transform toString
      trans[component] = transform;
      if (animation != null) animation.getLayer(component).setTransform(transform);
    }
  }

  public void setAlpha(byte component, float a) {
    if (alpha[component] != a) {
      if (DEBUG_STATE) Gdx.app.debug(TAG, classname + " alpha: " + alpha[component] + " -> " + a);
      alpha[component] = a;
      if (animation != null) {
        Animation.Layer layer = animation.getLayer(component);
        if (layer != null) layer.setAlpha(a);
      }
    }
  }

  public final void invalidate() {
    dirty = Dirty.ALL;
  }

  protected final void invalidate(int dirty) {
    this.dirty |= dirty;
  }

  public final void validate() {
    if (dirty == Dirty.NONE && load == Dirty.NONE) return;
    updateCOF();
    loadLayers();
  }

  protected void updateCOF() {
    this.cof = token + type.MODE[mode] + WCLASS[wclass];
    COF cof = type.getCOFs().lookup(this.cof);
    if (DEBUG_COF) Gdx.app.debug(TAG, this.cof + "=" + cof);

    boolean changed = updateAnimation(cof);
    if (changed) dirty = Dirty.ALL;
    if (dirty == Dirty.NONE) return;
    if (DEBUG_DIRTY) Gdx.app.debug(TAG, "dirty layers: " + Dirty.toString(dirty));
    load = Dirty.NONE; // TODO: unload this.layer assets

    final int start = type.PATH.length() + 4; // start after token
    StringBuilder builder = new StringBuilder(start + 19)
        .append(type.PATH).append('\\')
        .append(token).append('\\')
        .append("AA").append("\\")
        .append(token).append("AABBB").append(type.MODE[mode]).append("CCC").append(".dcc");
    for (int l = 0; l < cof.getNumLayers(); l++) {
      COF.Layer layer = cof.getLayer(l);
      if (!Dirty.isDirty(dirty, layer.component)) continue;
      if (comp[layer.component] == 0) { // should also ignore 0xFF which is -1
        this.layer[layer.component] = null;
        continue;
      } else if (comp[layer.component] == -1) {
        comp[layer.component] = 1;
      }

      String composit = COMPOSIT[layer.component];
      builder
          .replace(start     , start +  2, composit)
          .replace(start +  5, start +  7, composit)
          .replace(start +  7, start + 10, type.COMP[comp[layer.component]])
          .replace(start + 12, start + 15, layer.weaponClass);
      String path = builder.toString();
      if (DEBUG_DIRTY) Gdx.app.log(TAG, path);

      AssetDescriptor<? extends DC> descriptor = this.layer[layer.component] = new AssetDescriptor<>(path, DCC.class);
      Riiablo.assets.load(descriptor);
      load |= (1 << layer.component);
    }

    // TODO: This seems to work well with the default movement speeds of most entities I've seen
    if (mode == getWalkMode()) {
      animation.setFrameDelta(128);
    } else if (mode == getRunMode()) {
      animation.setFrameDelta(128);
    }

    dirty = Dirty.NONE;
  }

  protected void loadLayers() {
    if (load == Dirty.NONE) return;
    COF cof = type.getCOFs().lookup(this.cof);
    for (int l = 0; l < cof.getNumLayers(); l++) {
      COF.Layer layer = cof.getLayer(l);
      if (!Dirty.isDirty(load, layer.component)) continue;
      if (comp[layer.component] == 0) {
        load &= ~(1 << layer.component);
        animation.setLayer(layer, null, false);
        continue;
      }

      AssetDescriptor<? extends DC> descriptor = this.layer[layer.component];
      if (Riiablo.assets.isLoaded(descriptor)) {
        if (DEBUG_LOAD) Gdx.app.debug(TAG, "loaded " + descriptor);
        load &= ~(1 << layer.component);
        DC dc = Riiablo.assets.get(descriptor);
        animation.setLayer(layer, dc, false)
            .setTransform(trans[layer.component])
            .setAlpha(alpha[layer.component])
            ;
        // FIXME: colors don't look right for sorc Tirant circlet changing hair color
        //        putting a ruby in a white circlet not change color on item or character
        //        circlets and other items with hidden magic level might work different?
      }
    }

    animation.updateBox();
  }

  private boolean updateAnimation(COF cof) {
    if (animation == null) {
      animation = Animation.newAnimation(cof);
      animation.addAnimationListener(-1, this);
      updateDirection();
      return true;
    } else {
      return animation.reset(cof);
    }
  }

  @Override
  public void onTrigger(Animation animation, int frame) {
    switch (frame) {
      case -1: onAnimationFinished(animation); break;
      default: // do nothing
    }
  }

  protected void onAnimationFinished(Animation animation) {}

  public String getCOF() {
    return cof;
  }

  private void updateDirection() {
    if (animation != null) animation.setDirection(direction());
  }

  public void act(float delta) {
    EngineUtils.worldToScreenCoords(position, screen);
    if (overlay != null) overlay.act(delta);
    if (animation != null) animation.act(delta);
  }

  public void draw(PaletteIndexedBatch batch) {
    validate();
    if (load != Dirty.NONE) return;
    if (overlayEntry != null && overlayEntry.PreDraw) overlay.draw(batch, screen.x, screen.y);
    animation.draw(batch, screen.x, screen.y);
    if (overlayEntry != null && !overlayEntry.PreDraw) overlay.draw(batch, screen.x, screen.y);
    label.setPosition(screen.x, screen.y + getLabelOffset() + label.getHeight() / 2, Align.center);
    //if (animation.isFinished() && nextMode >= 0) {
    //  setMode(nextMode);
    //  nextMode = -1;
    //}
  }

  public void drawShadow(PaletteIndexedBatch batch) {
    validate();
    if (load != Dirty.NONE) return;
    animation.drawShadow(batch, screen.x, screen.y, false);
  }


  public void drawDebug(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    if (DEBUG_SIZE) drawDebugSize(shapes);
    if (DEBUG_STATUS) drawDebugStatus(batch, shapes);
    if (DEBUG_TARGET) drawDebugTarget(shapes);
  }

  public void drawDebugSize(ShapeRenderer shapes) {
    if (size < 1) return;
    shapes.set(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(Color.GRAY);
    int[][] SIZE = SIZES[size - 1];
    for (int[] subtile : SIZE) {
      EngineUtils.worldToScreenCoords(position.x + subtile[0], position.y + subtile[1], tmpVec2);
      DebugUtils.drawDiamond(shapes, tmpVec2.x, tmpVec2.y, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
    }
    shapes.set(ShapeRenderer.ShapeType.Line);
  }

  public void drawDebugStatus(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    float x = screen.x;
    float y = screen.y;
    if (animation != null && isSelectable()) animation.drawDebug(shapes, x, y);

    shapes.setColor(Color.WHITE);
    DebugUtils.drawDiamond(shapes, x, y, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
    //shapes.ellipse(x - Tile.SUBTILE_WIDTH50, y - Tile.SUBTILE_HEIGHT50, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);

    final float R = 32;
    shapes.setColor(Color.RED);
    shapes.line(x, y, x + MathUtils.cos(angle) * R, y + MathUtils.sin(angle) * R);

    if (animation != null) {
      int numDirs = animation.getNumDirections();
      float rounded = Direction.snapToDirection(angle, numDirs);
      shapes.setColor(Color.GREEN);
      shapes.line(x, y, x + MathUtils.cos(rounded) * R * 0.5f, y + MathUtils.sin(rounded) * R * 0.5f);
    }

    shapes.end();
    batch.begin();
    batch.setShader(null);
    StringBuilder builder = new StringBuilder(64)
        .append(classname).append('\n')
        .append(token).append(' ').append(type.MODE[mode]).append(' ').append(WCLASS[wclass]).append('\n');
    if (animation != null) {
      builder
          .append(StringUtils.leftPad(Integer.toString(animation.getFrame()), 2))
          .append('/')
          .append(StringUtils.leftPad(Integer.toString(animation.getNumFramesPerDir() - 1), 2))
          .append(' ')
          .append(animation.getFrameDelta())
          .append('\n');
    }
    appendToStatus(builder);
    GlyphLayout layout = Riiablo.fonts.consolas12.draw(batch, builder.toString(), x, y - Tile.SUBTILE_HEIGHT50, 0, Align.center, false);
    Pools.free(layout);
    batch.end();
    batch.setShader(Riiablo.shader);
    shapes.begin(ShapeRenderer.ShapeType.Line);
  }

  protected void appendToStatus(StringBuilder builder) {}

  public void drawDebugTarget(ShapeRenderer shapes) {
    if (target.isZero() || !path.isEmpty()) return;
    EngineUtils.worldToScreenCoords(target, tmpVec2);
    shapes.set(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(Color.ORANGE);
    shapes.rectLine(screen.x, screen.y, tmpVec2.x, tmpVec2.y, 1);
    shapes.set(ShapeRenderer.ShapeType.Line);
  }

  public void drawDebugPath(PaletteIndexedBatch batch, ShapeRenderer shapes) {}

  public Vector2 position() {
    return position;
  }

  public Vector2 screen() {
    return screen;
  }

  public boolean contains(Vector2 coords) {
    if (animation == null) return false;
    if (!isSelectable()) return false;
    BBox box = animation.getBox();
    float x = screen.x + box.xMin;
    float y = screen.y - box.yMax;
    return x <= coords.x && coords.x <= x + box.width
       &&  y <= coords.y && coords.y <= y + box.height;
  }

  public float angle() {
    return angle;
  }

  public void angle(float rad) {
    if (angle != rad) {
      angle = rad;
      updateDirection();
    }
  }

  public void lookAt(Entity entity) {
    lookAt(entity.screen.x, entity.screen.y);
  }

  public void lookAt(float x, float y) {
    tmpVec2.set(x, y).sub(screen);
    angle(MathUtils.atan2(tmpVec2.y, tmpVec2.x));
  }

  public int direction() {
    int numDirs = animation.getNumDirections();
    return Direction.radiansToDirection(angle, numDirs);
  }

  public float getInteractRange() {
    return -1;
  }

  public void interact(GameScreen gameScreen) {}

  public boolean isSelectable() {
    return false;
  }

  public String name() {
    return name;
  }

  public void name(String name) {
    this.name = name;
    if (label instanceof Label) {
      ((Label) label).setText(name);
    }
  }

  public Actor getLabel() {
    return label;
  }

  public float getLabelOffset() {
    return animation.getMinHeight();
  }

  public boolean isOver() {
    return over;
  }

  public void setOver(boolean b) {
    if (over != b) {
      over = b;
      if (animation != null) animation.setHighlighted(b);
    }
  }

  public boolean sequence(byte transition, byte mode) {
    boolean changed = this.mode != transition;
    setMode(transition);
    nextMode = mode;
    return changed;
  }

  public Vector2 target() {
    return target;
  }

  public MapGraph.MapGraphPath path() {
    return path;
  }

  public Iterator<MapGraph.Point2> targets() {
    return targets;
  }

  public boolean setPath(Map map, Vector2 dst) {
    return setPath(map, dst, -1);
  }

  public boolean setPath(Map map, Vector2 dst, int maxSteps) {
    if (dst == null) {
      path.clear();
      targets = Collections.emptyIterator();
      target.set(position);
      return false;
    }

    boolean success = map.findPath(position, dst, path);
    if (!success) return false;
    if (maxSteps != -1 && path.getCount() > maxSteps) {
      path.clear();
      targets = Collections.emptyIterator();
      return false;
    }

    if (DEBUG_PATH) Gdx.app.debug(TAG, "path=" + path);
    map.smoothPath(path);
    targets = new Array.ArrayIterator<>(path.nodes);
    targets.next(); // consume src position
    if (targets.hasNext()) {
      MapGraph.Point2 firstDst = targets.next();
      target.set(firstDst.x, firstDst.y);
    } else {
      target.set(position);
    }

    //if (DEBUG_TARGET) Gdx.app.debug(TAG, "target=" + target);
    return true;
  }

  public void setRunning(boolean b) {
    if (running != b) {
      running = b;
    }
  }

  public void setWalkSpeed(float speed) {
    if (walkSpeed != speed) {
      walkSpeed = speed;
    }
  }

  public void setRunSpeed(float speed) {
    if (runSpeed != speed) {
      runSpeed = speed;
    }
  }

  public byte getNeutralMode() {
    return 0;
  }

  public byte getWalkMode() {
    return getNeutralMode();
  }

  public byte getRunMode() {
    return getWalkMode();
  }

  public void update(float delta) {
    if (animation != null && animation.isFinished() && nextMode >= 0) {
      setMode(nextMode);
      nextMode = -1;
    }

    if (target.isZero()) return;
    if (position.epsilonEquals(target)) {
      if (!targets.hasNext()) {
        path.clear();
        if (isMoving(mode)) setMode(getNeutralMode());
        return;
      }
    }

    setMode(running ? getRunMode() : getWalkMode());
    float speed     = (running ? walkSpeed + runSpeed : walkSpeed);
    float distance  = speed * delta;
    float traveled  = 0;
    while (traveled < distance) {
      float targetLen = position.dst(target);
      float part = Math.min(distance - traveled, targetLen);
      if (part == 0) break;
      position.lerp(target, part / targetLen);
      traveled += part;
      if (part == targetLen) {
        if (targets.hasNext()) {
          MapGraph.Point2 next = targets.next();
          target.set(next.x, next.y);
        } else {
          break;
        }
      }
    }
  }

  public boolean isCasting(byte mode) {
    return false;
  }

  public boolean isMoving(byte mode) {
    return false;
  }

  public boolean isCastable(byte mode) {
    return false;
  }

  public boolean cast(final int spell) {
    if (spell < 0) return false;
    if (!isCastable(mode)) return false;
    setPath(null, null);
    //if (mode == getNeutralMode()) return;
    //animating = true;
    final Skills.Entry skill = Riiablo.files.skills.get(spell);
    byte tm = mode;
    byte newMode = type.getMode(skill.anim);
    // FIXME: NOTE: I'm think SQ (sequence) used by player spells are hard-coded and indicate
    //              something like cast + wait. It's possible this is used to block the player
    //              from using other spells, or somehow resetting the cooldown.
    if (newMode == -1) newMode = type.getMode("SC");
    boolean changed = sequence(newMode, getNeutralMode());
    if (!changed) return false;

    /*
    updateCOF(); // FIXME: required because we need updated COF to bind to trigger -- should really be done on next frame
    for (COF.Keyframe keyframe : COF.Keyframe.values()) {
      int frame = animation.getCOF().getKeyframeFrame(keyframe);
      System.out.println("keyframe[" + keyframe + "]=" + frame);
    }

    int frame = animation.getCOF().getKeyframeFrame(COF.Keyframe.ATTACK);
    if (frame >= 0) {
      animation.addAnimationListener(frame, new Animation.AnimationListener() {
        @Override
        public void onTrigger(Animation animation, int frame) {
          System.out.println("onTrigger " + frame + " " + COF.Keyframe.ATTACK);
          animation.removeAnimationListener(frame, this);
        }
      });
    }
    */

    System.out.println("cast " + type.MODE[tm] + "->" + type.MODE[mode]);
    Riiablo.audio.play(skill.stsound, true);

    if (!skill.castoverlay.isEmpty()) {
      overlayEntry = Riiablo.files.Overlay.get(skill.castoverlay);
      AssetDescriptor<DCC> descriptor = new AssetDescriptor<>("data\\global\\overlays\\" + overlayEntry.Filename + ".dcc", DCC.class);
      Riiablo.assets.load(descriptor);
      Riiablo.assets.finishLoadingAsset(descriptor);
      DCC dcc = Riiablo.assets.get(descriptor);
      overlay = Animation.builder()
          .layer(dcc, overlayEntry.Trans == 3 ? BlendMode.LUMINOSITY : BlendMode.ID)
          .build();
      overlay.setLooping(false);
      overlay.setClamp(false);
      //overlay.setFrameDuration(1f / overlayEntry.AnimRate);
    }

    return true;
  }

  public void setSize(int size) {
    if (this.size != size) {
      this.size = size;
    }
  }

  public int getSize() {
    return size;
  }
}
