package gdx.diablo.entity3;

import android.support.annotation.CallSuper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;

import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.COFD2;
import gdx.diablo.codec.DCC;
import gdx.diablo.entity.Direction;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.item.Item;
import gdx.diablo.map.DT1.Tile;

public class Entity {
  private static final String TAG = "Entity";
  private static final boolean DEBUG            = true;
  private static final boolean DEBUG_COMPONENTS = DEBUG && true;
  private static final boolean DEBUG_DIRTY      = DEBUG && true;
  private static final boolean DEBUG_ASSETS     = DEBUG && true;
  private static final boolean DEBUG_WCLASS     = DEBUG && true;

  protected enum EntType {
    OBJECT,
    MONSTER,
    CHARS;

    public final String PATH = "data\\global\\" + name() + "\\";
  }

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

    public static String toString(int bits) {
      StringBuilder builder = new StringBuilder();
      if (bits == NONE) {
        builder.append("NONE");
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
  }

  private final int ALL_DIRTY = 0x0000FFFF;
  protected static final String DEFAULT_LAYER = "LIT";
  private static final String[] DEFAULT_LAYERS;
  static {
    DEFAULT_LAYERS = new String[16];
    Arrays.fill(DEFAULT_LAYERS, DEFAULT_LAYER);
  }

  GridPoint2 origin = new GridPoint2();
  float      angle  = MathUtils.PI * 3 / 2;

  int    dirty;
  String type;
  String mode;
  String code;
  String layers[];
  String weaponClass;

  EntType entType;
  Animation animation;

  Entity(String type) {
    this(type, EntType.OBJECT);
  }

  Entity(String type, EntType entType) {
    this.type    = type;
    this.entType = entType;
    mode = code  = "NU";
    weaponClass  = "HTH";
    layers       = DEFAULT_LAYERS;
    invalidate();
  }

  public void setMode(String mode) {
    setMode(mode, mode);
  }

  public void setMode(String mode, String code) {
    if (!this.mode.equals(mode)) {
      this.mode = mode;
      invalidate();
    }

    this.code = code;
  }

  public void setWeaponClass(String wclass) {
    if (!weaponClass.equals(wclass)) {
      if (DEBUG_WCLASS) Gdx.app.debug(TAG, "wclass: " + weaponClass + " -> " + wclass);
      weaponClass = wclass;
      invalidate();
    }
  }

  public void setArmType(Component component, String armType) {
    if (layers == DEFAULT_LAYERS) {
      if (!armType.equalsIgnoreCase(DEFAULT_LAYER)) {
        layers = ArrayUtils.clone(DEFAULT_LAYERS);
      } else {
        return;
      }
    }

    int ordinal = component.ordinal();
    if (layers[ordinal].equalsIgnoreCase(armType)) {
      return;
    }

    if (DEBUG_COMPONENTS) Gdx.app.debug(TAG, "layer " + ordinal + " " + layers[ordinal] + " -> " + armType);
    layers[ordinal] = armType;
    dirty |= (1 << ordinal);
  }

  protected Item getItem(Component component) {
    return null;
  }

  public final void invalidate() {
    dirty = ALL_DIRTY;
  }

  public final void validate() {
    if (dirty == 0) {
      return;
    }

    update();
  }

  @CallSuper
  protected void update() {
    String path = type + mode + weaponClass;
    Gdx.app.log(TAG, path);

    COF cof = getCOFs().lookup(path);
    Gdx.app.log(TAG, ObjectUtils.toString(cof));

    boolean changed;
    if (animation == null) {
      animation = Animation.newAnimation(cof);
      changed = true;
    } else {
      changed = animation.reset(cof);
    }

    if (changed) {
      dirty = ALL_DIRTY;
      animation.setDirection(getDirection());
    }

    if (DEBUG_DIRTY) Gdx.app.debug(TAG, "dirty layers: " + Dirty.toString(dirty));
    for (int l = 0; l < cof.getNumLayers(); l++) {
      COF.Layer layer = cof.getLayer(l);
      if (((1 << layer.component) & dirty) == 0) {
        continue;
      }

      final int c = layer.component;
      final Component comp = Component.valueOf(c);
      if (comp == null) continue;
      String component   = comp.name();
      String armType     = layers[c];
      String weaponClass = layer.weaponClass;
      path = entType.PATH + type + "\\" + component + "\\" + type + component + armType + mode + weaponClass + ".dcc";
      if (armType.isEmpty()) {
        animation.setLayer(c, null);
        continue;
      }
      Gdx.app.log(TAG, path);

      if (DEBUG_ASSETS) {
        AssetDescriptor<DCC> descriptor = new AssetDescriptor<>(path, DCC.class);
        Diablo.assets.load(descriptor);
        Diablo.assets.finishLoadingAsset(descriptor);
        DCC dcc = Diablo.assets.get(descriptor);
        animation.setLayer(c, dcc);
      }

      Item item = getItem(comp);
      if (item != null) {
        animation.getLayer(layer.component).setTransform(item.charColormap, item.charColorIndex);
      }
    }

    dirty = 0;
  }

  protected COFD2 getCOFs() {
    return Diablo.cofs.active;
  }

  public void drawDebug(ShapeRenderer shapes) {
    float x = +(origin.x * Tile.SUBTILE_WIDTH50)  - (origin.y * Tile.SUBTILE_WIDTH50);
    float y = -(origin.x * Tile.SUBTILE_HEIGHT50) - (origin.y * Tile.SUBTILE_HEIGHT50);

    final float R = 32;
    shapes.setColor(Color.RED);
    shapes.line(x, y, x + MathUtils.cos(angle) * R, y + MathUtils.sin(angle) * R);

    float rounded = Direction.radiansToDirection16Radians(angle);
    shapes.setColor(Color.GREEN);
    shapes.line(x, y, x + MathUtils.cos(rounded) * R * 0.5f, y + MathUtils.sin(rounded) * R * 0.5f);
  }

  public void draw(Batch batch) {
    draw((PaletteIndexedBatch) batch);
  }

  public void draw(PaletteIndexedBatch batch) {
    validate();
    animation.act();
    float x = +(origin.x * Tile.SUBTILE_WIDTH50)  - (origin.y * Tile.SUBTILE_WIDTH50);
    float y = -(origin.x * Tile.SUBTILE_HEIGHT50) - (origin.y * Tile.SUBTILE_HEIGHT50);
    animation.draw(batch, x, y);
  }

  public void setAngle(float rad) {
    if (angle != rad) {
      angle = rad;
      if (animation != null) animation.setDirection(getDirection());
    }
  }

  public int getDirection() {
    return Direction.radiansToDirection(angle, 16);
  }

  public GridPoint2 origin() {
    return origin;
  }

  public void move() {
    if (!mode.equalsIgnoreCase("WL")
     && !mode.equalsIgnoreCase("RN")
     && !mode.equalsIgnoreCase("TW")) {
      return;
    }

    int x = Direction.getOffX(angle);
    int y = Direction.getOffY(angle);
    origin.add(x, y);
  }
}
