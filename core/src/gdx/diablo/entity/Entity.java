package gdx.diablo.entity;

import android.support.annotation.CallSuper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.COFD2;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.util.BBox;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.graphics.PaletteIndexedColorDrawable;
import gdx.diablo.map.DS1;
import gdx.diablo.map.DT1.Tile;
import gdx.diablo.map.Map;
import gdx.diablo.map.MapGraph;
import gdx.diablo.map.MapRenderer;
import gdx.diablo.screen.GameScreen;
import gdx.diablo.widget.Label;

public class Entity {
  private static final String TAG = "Entity";
  private static final boolean DEBUG            = true;
  private static final boolean DEBUG_COMPONENTS = DEBUG && true;
  private static final boolean DEBUG_COF        = DEBUG && !true;
  private static final boolean DEBUG_DIRTY      = DEBUG && !true;
  private static final boolean DEBUG_ASSETS     = DEBUG && true;
  private static final boolean DEBUG_STATE      = DEBUG && true;
  private static final boolean DEBUG_PATH       = DEBUG && !true;
  private static final boolean DEBUG_TARGET     = DEBUG && true;

  protected enum EntType {
    OBJECT("OBJECTS"),
    MONSTER("MONSTERS"),
    PLAYER("CHARS");

    public final String PATH;

    EntType(String path) {
      PATH = "data\\global\\" + path + "\\";
    }
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

  protected static final String DEFAULT_LAYER = "LIT";
  private static final String[] DEFAULT_LAYERS;
  static {
    DEFAULT_LAYERS = new String[16];
    Arrays.fill(DEFAULT_LAYERS, DEFAULT_LAYER);
  }

  String  type;
  EntType entType;
  String  className;

  int     dirty;
  String  mode;
  String  code;
  String  layers[];
  String  weaponClass;
  Vector3 position = new Vector3();
  Vector3 velocity = new Vector3();
  float   angle    = MathUtils.atan2(-1, -2);

  boolean running   = false;
  float   walkSpeed = 6;
  float   runSpeed  = 9;

  Animation animation;
  public boolean   over = true;
  Label label;
  String name;
  Vector3 target = new Vector3();
  MapGraph.MapGraphPath path = new MapGraph.MapGraphPath();
  Iterator<MapGraph.Point2> targets = Collections.emptyIterator();

  private static final Vector2 tmpVec2 = new Vector2();

  public static Entity create(Map map, Map.Zone zone, DS1 ds1, DS1.Object obj) {
    final int type = obj.type;
    switch (type) {
      case DS1.Object.DYNAMIC_TYPE:
        return Monster.create(map, zone, ds1, obj);
      case DS1.Object.STATIC_TYPE:
        return StaticEntity.create(map, zone, ds1, obj);
      default:
        throw new AssertionError("Unexpected type: " + type);
    }
  }

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

    // TODO: LabelStyle should be made static
    label = new Label(Diablo.fonts.font16);
    label.setUserObject(this);
    label.setAlignment(Align.center);
    label.getStyle().background = new PaletteIndexedColorDrawable(Diablo.colors.modal75) {{
      final float padding = 2;
      setLeftWidth(padding);
      setTopHeight(padding);
      setRightWidth(padding);
      setBottomHeight(padding);
    }};
  }

  public void setMode(String mode) {
    setMode(mode, mode);
  }

  public void setMode(String mode, String code) {
    if (!this.mode.equalsIgnoreCase(mode)) {
      if (DEBUG_STATE) Gdx.app.debug(TAG, className + " " + "mode: " + this.mode + " -> " + mode);
      this.mode = mode;
      invalidate();
    }

    this.code = code;
  }

  public void setWeaponClass(String weaponClass) {
    if (!this.weaponClass.equalsIgnoreCase(weaponClass)) {
      if (DEBUG_STATE) Gdx.app.debug(TAG, className + " " + "weaponClass: " + this.weaponClass + " -> " + weaponClass);
      this.weaponClass = weaponClass;
      invalidate();
    }
  }

  public void setArmType(Component component, String armType) {
    if (layers == DEFAULT_LAYERS) {
      if (!DEFAULT_LAYER.equalsIgnoreCase(armType)) {
        layers = ArrayUtils.clone(DEFAULT_LAYERS);
      } else {
        return;
      }
    }

    int ordinal = component.ordinal();
    if (layers[ordinal].equalsIgnoreCase(armType)) {
      return;
    }

    if (DEBUG_COMPONENTS) Gdx.app.debug(TAG, component + " " + layers[ordinal] + " -> " + armType);
    layers[ordinal] = armType;
    dirty |= (1 << ordinal);
  }

  protected byte getTransform(Component component) {
    return (byte) 0xFF;
  }

  public Vector3 position() {
    return position;
  }

  public Vector3 velocity() {
    return velocity;
  }

  public Vector3 target() {
    return target;
  }

  public MapGraph.MapGraphPath path() {
    return path;
  }

  public Iterator<MapGraph.Point2> targets() {
    return targets;
  }

  public boolean isRunning() {
    return running;
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

  public boolean setPath(Map map, Vector3 dst) {
    return setPath(map, dst, -1);
  }

  public boolean setPath(Map map, Vector3 dst, int maxSteps) {
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
      target.set(firstDst.x, firstDst.y, 0);
    } else {
      target.set(position);
    }

    //if (DEBUG_TARGET) Gdx.app.debug(TAG, "target=" + target);
    return true;
  }

  public void update(float delta) {
    if (target.equals(Vector3.Zero)) return;
    if (position.epsilonEquals(target)) {
      if (!targets.hasNext()) {
        path.clear();
        if (mode.equalsIgnoreCase(running ? "RN" : "WL")) setMode("NU");
        return;
      }
    }

    setMode(running ? "RN" : "WL");
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
          target.set(next.x, next.y, 0);
        } else {
          break;
        }
      }
    }
  }

  public float getAngle() {
    return angle;
  }

  public void setAngle(float rad) {
    if (angle != rad) {
      angle = rad;
      if (animation != null) animation.setDirection(getDirection());
    }
  }

  public int getDirection() {
    int numDirs = animation.getNumDirections();
    return Direction.radiansToDirection(angle, numDirs);
  }

  public void lookAt(Entity entity) {
    float x1 = +(entity.position.x * Tile.SUBTILE_WIDTH50)  - (entity.position.y * Tile.SUBTILE_WIDTH50);
    float y1 = -(entity.position.x * Tile.SUBTILE_HEIGHT50) - (entity.position.y * Tile.SUBTILE_HEIGHT50);
    float x2 = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    float y2 = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    tmpVec2.set(x1, y1).sub(x2, y2);
    setAngle(MathUtils.atan2(tmpVec2.y, tmpVec2.x));
  }

  public final void invalidate() {
    dirty = Dirty.ALL;
  }

  public final void validate() {
    if (dirty == 0) {
      return;
    }

    update();
  }

  @CallSuper
  protected void update() {
    String path = getCOF();
    //Gdx.app.debug(TAG, path);

    COF cof = getCOFs().lookup(path);
    if (DEBUG_COF) Gdx.app.debug(TAG, "" + cof);

    boolean changed = updateAnimation(cof);
    if (changed) dirty = Dirty.ALL;
    if (dirty == Dirty.NONE) return;

    if (DEBUG_DIRTY) Gdx.app.debug(TAG, "dirty layers: " + Dirty.toString(dirty));
    for (int l = 0; l < cof.getNumLayers(); l++) {
      COF.Layer layer = cof.getLayer(l);
      final int c = layer.component;
      if (!Dirty.isDirty(dirty, c)) {
        continue;
      }

      final Component comp = Component.valueOf(c);
      if (comp == null) continue;
      String component   = comp.name();
      String armType     = layers[c];
      String weaponClass = layer.weaponClass;
      path = entType.PATH + type + "\\" + component + "\\" + type + component + armType + mode + weaponClass + ".dcc";
      if (armType.isEmpty()) {
        animation.setLayer(c, null, false);
        continue;
      }
      if (DEBUG_DIRTY) Gdx.app.log(TAG, path);

      AssetDescriptor<DCC> descriptor = new AssetDescriptor<>(path, DCC.class);
      Diablo.assets.load(descriptor);
      Diablo.assets.finishLoadingAsset(descriptor);
      DCC dcc = Diablo.assets.get(descriptor);
      animation.setLayer(c, dcc, false);

      /*Runnable loader = new Runnable() {
        @Override
        public void run() {
          if (!Diablo.assets.isLoaded(descriptor)) {
            Gdx.app.postRunnable(this);
            return;
          }

          DCC dcc = Diablo.assets.get(descriptor);
          animation.setLayer(c, dcc);

          Item item = getItem(comp);
          if (item != null) {
            animation.getLayer(c).setTransform(item.charColormap, item.charColorIndex);
          }
        }
      };*/
      //Gdx.app.postRunnable(loader);

      byte transform = getTransform(comp);
      animation.getLayer(c).setTransform(transform);
      /*
      if (item != null) {
        // FIXME: colors don't look right for sorc Tirant circlet changing hair color
        //        putting a ruby in a white circlet not change color on item or character
        //        circlets and other items with hidden magic level might work different?
        animation.getLayer(layer.component).setTransform(item.charColormap, item.charColorIndex);
        //System.out.println(item.getName() + ": " + item.charColormap + " ; " + item.charColorIndex);
      }
      */
    }

    // TODO: This seems to work well with the default movement speeds of most entities I've seen
    if (mode.equalsIgnoreCase("WL")) {
      animation.setFrameDelta(128);
    } else if (mode.equalsIgnoreCase("RN")) {
      animation.setFrameDelta(128);
    }

    animation.updateBox();
    dirty = 0;
  }

  private boolean updateAnimation(COF cof) {
    if (animation == null) {
      animation = Animation.newAnimation(cof);
      animation.setDirection(getDirection());
      return true;
    } else {
      return animation.reset(cof);
    }
  }

  public String getCOF() {
    return type + mode + weaponClass;
  }

  protected COFD2 getCOFs() {
    return Diablo.cofs.active;
  }

  public void drawDebug(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    drawDebugStatus(batch, shapes);
    if (DEBUG_TARGET) drawDebugTarget(shapes);
  }

  public void drawDebugStatus(PaletteIndexedBatch batch, ShapeRenderer shapes) {
    float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    if (animation != null && isSelectable()) animation.drawDebug(shapes, x, y);

    shapes.setColor(Color.WHITE);
    MapRenderer.drawDiamond(shapes, x - Tile.SUBTILE_WIDTH50, y - Tile.SUBTILE_HEIGHT50, Tile.SUBTILE_WIDTH, Tile.SUBTILE_HEIGHT);
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
    Diablo.fonts.consolas12.draw(batch, getClassName() + "\n" + type + " " + mode + " " + weaponClass, x, y - Tile.SUBTILE_HEIGHT50, 0, Align.center, false);
    batch.end();
    batch.setShader(Diablo.shader);
    shapes.begin(ShapeRenderer.ShapeType.Line);
  }

  public void drawDebugTarget(ShapeRenderer shapes) {
    if (target.equals(Vector3.Zero) || !path.isEmpty()) return;
    float srcX = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    float srcY = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    float dstX = +(target.x * Tile.SUBTILE_WIDTH50)  - (target.y * Tile.SUBTILE_WIDTH50);
    float dstY = -(target.x * Tile.SUBTILE_HEIGHT50) - (target.y * Tile.SUBTILE_HEIGHT50);
    shapes.set(ShapeRenderer.ShapeType.Filled);
    shapes.setColor(Color.ORANGE);
    shapes.rectLine(srcX, srcY, dstX, dstY, 1);
    shapes.set(ShapeRenderer.ShapeType.Line);
  }

  public void drawDebugPath(PaletteIndexedBatch batch, ShapeRenderer shapes) {}

  public void draw(Batch batch) {
    draw((PaletteIndexedBatch) batch);
  }

  public void draw(PaletteIndexedBatch batch) {
    validate();
    animation.act();
    float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    animation.draw(batch, x, y);
    updateLabel(label, x, y);
    //if (over) drawLabel(batch);
  }

  protected void updateLabel(Label label, float x, float y) {
    label.setPosition(x, y + getLabelOffset() + label.getHeight() / 2, Align.center);
  }

  public float getLabelOffset() {
    return animation.getMinHeight();
  }

  /*
  public void drawLabel(PaletteIndexedBatch batch) {
    if (label.getText().length == 0) return;
    float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    label.setPosition(x, y + animation.getMinHeight() + label.getHeight(), Align.center);
    label.draw(batch, 1);
  }
  */

  public Label getLabel() {
    if (label.getText().length == 0) return null;
    return label;
  }

  public void drawShadow(PaletteIndexedBatch batch) {
    if (animation == null) return; // FIXME: drawing previous frame because animation not updated until draw is called
    float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50);
    float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50);
    animation.drawShadow(batch, x, y, false);
  }

  public boolean contains(Vector2 coords) {
    if (animation == null) return false;
    BBox box = animation.getBox();
    float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50)  + box.xMin;
    float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50) - box.yMax;
    return x <= coords.x && coords.x <= x + box.width
       &&  y <= coords.y && coords.y <= y + box.height;
  }

  public boolean contains(Vector3 coords) {
    if (animation == null) return false;
    BBox box = animation.getBox();
    float x = +(position.x * Tile.SUBTILE_WIDTH50)  - (position.y * Tile.SUBTILE_WIDTH50)  + box.xMin;
    float y = -(position.x * Tile.SUBTILE_HEIGHT50) - (position.y * Tile.SUBTILE_HEIGHT50) - box.yMax;
    return x <= coords.x && coords.x <= x + box.width
       &&  y <= coords.y && coords.y <= y + box.height;
  }

  public void setName(String name) {
    if (!StringUtils.equalsIgnoreCase(this.name, name)) {
      label.setText(this.name = name);
    }
  }

  public String getName() {
    return name;
  }

  public String getClassName() {
    return className;
  }

  public float getInteractRange() {
    return -1;
  }

  public void interact(GameScreen gameScreen) {}

  public boolean isSelectable() {
    return false;
  }
}
