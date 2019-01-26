package gdx.diablo.entity;

import com.google.common.base.Preconditions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import gdx.diablo.CharClass;
import gdx.diablo.Diablo;
import gdx.diablo.codec.Animation;
import gdx.diablo.codec.COF;
import gdx.diablo.codec.D2S;
import gdx.diablo.codec.DCC;
import gdx.diablo.codec.excel.Armor;
import gdx.diablo.codec.excel.ItemEntry;
import gdx.diablo.codec.excel.PlrMode;
import gdx.diablo.codec.excel.PlrType;
import gdx.diablo.codec.excel.WeaponClass;
import gdx.diablo.codec.excel.Weapons;
import gdx.diablo.graphics.PaletteIndexedBatch;
import gdx.diablo.item.BodyLoc;
import gdx.diablo.item.Item;

public class Player {
  private static final String TAG = "Player";
  private static final boolean DEBUG           = true;
  private static final boolean DEBUG_COF       = DEBUG && !true;
  private static final boolean DEBUG_EQUIPPED  = DEBUG && true;
  private static final boolean DEBUG_INVENTORY = DEBUG && true;

  private static final String CHARS = "data\\global\\chars\\";

  public static final int MAX_NAME_LENGTH = 15;

  D2S        d2s;
  String     name;
  GridPoint2 origin;
  float      angle;

  int plrModeId;
  PlrMode.Entry plrMode;

  int plrTypeId;
  PlrType.Entry plrType;

  //int weaponClassId;
  WeaponClass.Entry weaponClass;

  boolean dirty;
  String cofId;
  Animation anim;

  EnumMap<BodyLoc, Item> equipped;
  Array<Item> inventory;
  boolean usingAlternate;
  final Set<SlotListener> SLOT_LISTENERS = new CopyOnWriteArraySet<>();

  public Player(D2S d2s) {
    this.d2s = d2s;
    name      = d2s.name;
    plrTypeId = d2s.charClass;
    plrType   = Diablo.files.PlrType.get(plrTypeId);
    origin    = new GridPoint2();
    init();
    //setWeaponClass("hth");

    equipped = d2s.items.equipped;
    for (Map.Entry<BodyLoc, Item> entry : equipped.entrySet()) {
      entry.getValue().load();
      if (DEBUG_EQUIPPED) Gdx.app.debug(TAG, entry.getKey() + ": " + entry.getValue());
    }

    inventory = d2s.items.inventory;
    for (Item item : inventory) {
      item.load();
      if (DEBUG_INVENTORY) Gdx.app.debug(TAG, item.gridX + "," + item.gridY + ": " + item);
    }
  }

  public Player(String name, CharClass clazz) {
    this.name = name;
    plrTypeId = clazz.id;
    plrType   = Diablo.files.PlrType.get(plrTypeId);
    origin    = new GridPoint2();
    init();
    //setWeaponClass("hth");
    dirty = true;
  }

  private void init() {
    setMode("TN");
    setAngle(MathUtils.PI * 3 / 2);
  }

  public int getClassId() {
    return d2s.charClass;
  }

  public CharClass getCharClass() {
    return CharClass.get(d2s.charClass);
  }

  public String getName() {
    return d2s.name;
  }

  public int getLevel() {
    return d2s.stats.level;
  }

  public long getExperience() {
    return d2s.stats.xp;
  }

  public int getStrength() {
    return d2s.stats.strength;
  }

  public int getDexterity() {
    return d2s.stats.dexterity;
  }

  public int getVitality() {
    return d2s.stats.vitality;
  }

  public int getEnergy() {
    return d2s.stats.energy;
  }

  public int getFireResistance() {
    return 0;
  }

  public int getColdResistance() {
    return 0;
  }

  public int getLightningResistance() {
    return 0;
  }

  public int getPoisonResistance() {
    return 0;
  }

  public Item getBodyLoc(BodyLoc bodyLoc) {
    return equipped.get(bodyLoc);
  }

  public Item setBodyLoc(BodyLoc bodyLoc, Item item) {
    Preconditions.checkState(item == null || getBodyLoc(bodyLoc) == null, "Slot must be empty first!");
    Item oldItem = equipped.put(bodyLoc, item);
    for (SlotListener l : SLOT_LISTENERS) l.onChanged(this, bodyLoc, oldItem, item);
    return oldItem;
  }

  public boolean addSlotListener(SlotListener l) {
    boolean added = SLOT_LISTENERS.add(l);
    return added;
  }

  public boolean containsSlotListener(Object o) {
    return o != null && SLOT_LISTENERS.contains(o);
  }

  public boolean removeSlotListener(Object o) {
    return o != null && SLOT_LISTENERS.remove(o);
  }

  public boolean clearSlotListeners() {
    boolean empty = SLOT_LISTENERS.isEmpty();
    SLOT_LISTENERS.clear();
    return !empty;
  }

  public Item getComponentSlot(int component) {
    switch (component) {
      case COF.Component.HD: return getBodyLoc(BodyLoc.HEAD);
      case COF.Component.TR:
      case COF.Component.RA:
      case COF.Component.LA:
      case COF.Component.S1:
      case COF.Component.S2: return getBodyLoc(BodyLoc.TORS);
      // TODO: Shield/weapons?
      default:               return null;
    }
  }

  public Array<Item> getInventory() {
    return inventory;
  }

  public boolean isAlternate() {
    return usingAlternate;
  }

  public void setAlternate(boolean b) {
    if (usingAlternate != b) {
      usingAlternate = b;
    }
  }

  public static Player obtain(D2S d2s) {
    return new Player(d2s);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("name", name)
        .append("origin", origin)
        .append("plrMode", plrMode)
        .append("plrType", plrType)
        .append("weaponClass", weaponClass)
        .build();
  }

  public void setMode(String code) {
    setMode(Diablo.files.PlrMode.index(code));
  }

  public void setMode(int plrModeId) {
    if (this.plrModeId != plrModeId || plrMode == null) {
      this.plrModeId = plrModeId;
      plrMode = Diablo.files.PlrMode.get(plrModeId);
      dirty = true;
    }
  }

  /*
  public void setWeaponClass(String code) {
    setWeaponClass(Diablo.files.WeaponClass.index(code));
  }

  public void setWeaponClass(int weaponClassId) {
    if (this.weaponClassId != weaponClassId || weaponClass == null) {
      this.weaponClassId = weaponClassId;
      weaponClass = Diablo.files.WeaponClass.get(weaponClassId);
      dirty = true;
    }
  }
  */

  public void setAngle(float rad) {
    if (this.angle != rad) {
      this.angle = rad;
      if (anim != null) anim.setDirection(getDirection());
    }
  }

  public int getDirection() {
    return Direction.radiansToDirection(angle, 16);
  }

  public GridPoint2 getOrigin() {
    return origin;
  }

  public void update() {
    if (!dirty) {
      return;
    }

    dirty = false;

    String[] components = new String[COF.Component.NUM_COMPONENTS];
    Item rHand, lHand;
    if (!usingAlternate) {
      rHand = getBodyLoc(BodyLoc.RARM);
      lHand = getBodyLoc(BodyLoc.LARM);
    } else {
      rHand = getBodyLoc(BodyLoc.RARM2);
      lHand = getBodyLoc(BodyLoc.LARM2);
    }

    // TODO: custom code for barbarian _1or2handed
    if (rHand != null) {
      ItemEntry entry = rHand.base;
      components[entry.component] = entry.alternateGfx;
      if (entry instanceof Weapons.Entry) {
        weaponClass = Diablo.files.WeaponClass.get(((Weapons.Entry) entry).wclass);
      }
    }
    if (lHand != null) {
      ItemEntry entry = lHand.base;
      components[entry.component] = entry.alternateGfx;
      if (entry instanceof Weapons.Entry) {
        weaponClass = Diablo.files.WeaponClass.get(((Weapons.Entry) entry).wclass);
      }
    }
    if (weaponClass == null) {
      weaponClass = Diablo.files.WeaponClass.get("hth");
    }

    Item head = getBodyLoc(BodyLoc.HEAD);
    components[COF.Component.HD] = head != null ? head.base.alternateGfx : null;

    Item body = getBodyLoc(BodyLoc.TORS);
    if (body != null) {
      Armor.Entry armor = body.getBase();
      components[COF.Component.TR] = Diablo.files.ArmType.get(armor.Torso).Token;
      components[COF.Component.LG] = Diablo.files.ArmType.get(armor.Legs).Token;
      components[COF.Component.RA] = Diablo.files.ArmType.get(armor.rArm).Token;
      components[COF.Component.LA] = Diablo.files.ArmType.get(armor.lArm).Token;
      components[COF.Component.S1] = Diablo.files.ArmType.get(armor.lSPad).Token;
      components[COF.Component.S2] = Diablo.files.ArmType.get(armor.rSPad).Token;
    } else {
      components[COF.Component.TR] =
      components[COF.Component.LG] =
      components[COF.Component.RA] =
      components[COF.Component.LA] =
      components[COF.Component.S1] =
      components[COF.Component.S2] = "lit";
    }

    String cofId = plrType.Token + plrMode.Token + weaponClass.Code;
    if (DEBUG_COF) Gdx.app.debug(TAG, "COF: " + this.cofId + " -> " + cofId);
    COF cof = Diablo.cofs.chars_cof.lookup(cofId);
    this.cofId = cofId;

    // FIXME: dispose/unload old animation layer
    //if (animation != null) animation.dispose();

    Animation oldAnim = anim;
    anim = Animation.newAnimation(cof);
    // TODO: This might be a problem
    anim.setDirection(oldAnim != null ? oldAnim.getDirection() : getDirection());

    for (int i = 0; i < cof.getNumLayers(); i++) {
      COF.Layer layer = cof.getLayer(i);
      String component  = Diablo.files.Composit.get(layer.component).Token;
      String armorClass = components[layer.component];
      if (armorClass == null) continue;

      String weaponClass = layer.weaponClass;
      String path = CHARS + plrType.Token + "\\" + component + "\\" + plrType.Token + component + armorClass + plrMode.Token + weaponClass + ".dcc";

      AssetDescriptor<DCC> descriptor = new AssetDescriptor<>(path, DCC.class);
      Diablo.assets.load(descriptor);
      Diablo.assets.finishLoadingAsset(descriptor);
      DCC dcc = Diablo.assets.get(descriptor);
      anim.setLayer(layer.component, dcc);

      Item item = getComponentSlot(layer.component);
      if (item != null) {
        anim.getLayer(layer.component).setTransform(item.charColormap, item.charColorIndex);
        /*
        int trans = item.charTransformation;
        if (trans != 0xFFFFFFFF) {
          anim.getLayer(layer.component).setTransform(trans >>> 8);
          anim.getLayer(layer.component).setTransformColor(trans & 0xFF);
        }
        */
      }
    }
  }

  public void move() {
    switch (plrModeId) {
      case 2: case 3: case 6:
        break;
      default:
        return;
    }

    int x = Direction.getOffX(angle);
    int y = Direction.getOffY(angle);
    origin.add(x, y);
  }

  public void draw(PaletteIndexedBatch batch, int x, int y) {
    update();
    anim.act();
    anim.draw(batch, x, y);
  }

  public void drawDebug(ShapeRenderer shapes, int x, int y) {
    final float R = 32;
    shapes.setColor(Color.RED);
    shapes.line(x, y, x + MathUtils.cos(angle) * R, y + MathUtils.sin(angle) * R);

    float rounded = Direction.radiansToDirection16Radians(angle);
    shapes.setColor(Color.GREEN);
    shapes.line(x, y, x + MathUtils.cos(rounded) * R * 0.5f, y + MathUtils.sin(rounded) * R * 0.5f);
  }

  public interface SlotListener {
    void onChanged(Player player, BodyLoc bodyLoc, Item oldItem, Item item);
  }
}
