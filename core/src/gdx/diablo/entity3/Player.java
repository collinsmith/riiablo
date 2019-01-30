package gdx.diablo.entity3;

import com.google.common.base.Preconditions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.apache.commons.lang3.ObjectUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import gdx.diablo.CharClass;
import gdx.diablo.Diablo;
import gdx.diablo.codec.COFD2;
import gdx.diablo.codec.D2S;
import gdx.diablo.codec.excel.Armor;
import gdx.diablo.codec.excel.Weapons;
import gdx.diablo.item.BodyLoc;
import gdx.diablo.item.Item;

public class Player extends Entity {
  private static final String TAG = "Player";
  private static final boolean DEBUG        = true;
  private static final boolean DEBUG_WCLASS = DEBUG && !true;

  public enum Slot {
    HEAD, NECK, TORS, RARM, LARM, RRIN, LRIN, BELT, FEET, GLOV;

    public BodyLoc toBodyLoc(boolean alternate) {
      return toBodyLoc(this, alternate);
    }

    public static BodyLoc toBodyLoc(Slot slot, boolean alternate) {
      switch (slot) {
        case HEAD: return BodyLoc.HEAD;
        case NECK: return BodyLoc.NECK;
        case TORS: return BodyLoc.TORS;
        case RARM: return alternate ? BodyLoc.RARM2 : BodyLoc.RARM;
        case LARM: return alternate ? BodyLoc.LARM2 : BodyLoc.LARM;
        case RRIN: return BodyLoc.RRIN;
        case LRIN: return BodyLoc.LRIN;
        case BELT: return BodyLoc.BELT;
        case FEET: return BodyLoc.FEET;
        case GLOV: return BodyLoc.GLOV;
        default:
          throw new GdxRuntimeException("Invalid slot: " + slot);
      }
    }
  }

  D2S d2s;
  boolean alternate;
  EnumMap<BodyLoc, Item> equipped = new EnumMap<>(BodyLoc.class);
  Array<Item> inventory = new Array<>();
  final Set<SlotListener> SLOT_LISTENERS = new CopyOnWriteArraySet<>();
  public Stats stats;


  public Player(String name, CharClass clazz) {
    super(null);
    throw new UnsupportedOperationException();
  }

  public Player(D2S d2s) {
    super(Diablo.files.PlrType.get(d2s.charClass).Token, EntType.CHARS);
    this.d2s = d2s;
    this.stats = new Stats();
    equipped.putAll(d2s.items.equipped);
    inventory.addAll(d2s.items.inventory);
    setMode("TN");

    for (Map.Entry<BodyLoc, Item> entry : equipped.entrySet()) {
      entry.getValue().load();
      //if (DEBUG_EQUIPPED) Gdx.app.debug(TAG, entry.getKey() + ": " + entry.getValue());
    }

    for (Item item : inventory) {
      item.load();
      //if (DEBUG_INVENTORY) Gdx.app.debug(TAG, item.gridX + "," + item.gridY + ": " + item);
    }
  }

  public Item getSlot(Slot slot) {
    BodyLoc loc = slot.toBodyLoc(alternate);
    return getSlot(loc);
  }

  public Item getSlot(BodyLoc loc) {
    return equipped.get(loc);
  }

  public Item setSlot(Slot slot, Item item) {
    Preconditions.checkState(item == null || getSlot(slot) == null, "Slot must be empty first!");
    BodyLoc loc = slot.toBodyLoc(alternate);
    return setSlot(loc, item);
  }

  public Item setSlot(BodyLoc loc, Item item) {
    Item oldItem = equipped.put(loc, item);

    //invalidate();
    //setArmType(slot, item.base.alternateGfx);
    int components = loc.components();
    if (components > 0) dirty |= components;
    updateWeaponClass();

    notifySlotChanged(loc, oldItem, item);
    return oldItem;
  }

  @Override
  protected Item getItem(Component component) {
    switch (component) {
      case HD: return getSlot(Slot.HEAD);
      case TR:
      case RA:
      case LA:
      case S1:
      case S2: return getSlot(Slot.TORS);
      // TODO: Shield/weapons?
      default: return super.getItem(component);
    }
  }

  public boolean isAlternate() {
    return alternate;
  }

  public void setAlternate(boolean b) {
    if (alternate != b) {
      alternate = b;
      updateWeaponClass();
      Item LH = getSlot(BodyLoc.LARM);
      Item RH = getSlot(BodyLoc.RARM);
      Item LH2 = getSlot(BodyLoc.LARM2);
      Item RH2 = getSlot(BodyLoc.RARM2);
      if (b) {
        notifyAlternate(LH2, RH2);
      } else {
        notifyAlternate(LH, RH);
      }
    }
  }

  @Override
  protected COFD2 getCOFs() {
    return Diablo.cofs.chars_cof;
  }

  public void update() {
    updateWeaponClass();

    Item head = getSlot(Slot.HEAD);
    setArmType(Component.HD, head != null ? head.base.alternateGfx : "LIT");

    Item body = getSlot(Slot.TORS);
    if (body != null) {
      Armor.Entry armor = body.getBase();
      setArmType(Component.TR, Diablo.files.ArmType.get(armor.Torso).Token);
      setArmType(Component.LG, Diablo.files.ArmType.get(armor.Legs ).Token);
      setArmType(Component.RA, Diablo.files.ArmType.get(armor.rArm ).Token);
      setArmType(Component.LA, Diablo.files.ArmType.get(armor.lArm ).Token);
      setArmType(Component.S1, Diablo.files.ArmType.get(armor.lSPad).Token);
      setArmType(Component.S2, Diablo.files.ArmType.get(armor.rSPad).Token);
    } else {
      setArmType(Component.TR, DEFAULT_LAYER);
      setArmType(Component.LG, DEFAULT_LAYER);
      setArmType(Component.RA, DEFAULT_LAYER);
      setArmType(Component.LA, DEFAULT_LAYER);
      setArmType(Component.S1, DEFAULT_LAYER);
      setArmType(Component.S2, DEFAULT_LAYER);
    }

    super.update();
  }

  private void updateWeaponClass() {
    Item RH = null, LH = null, SH = null;
    Item rArm = getSlot(Slot.RARM);
    if (rArm != null) {
      if (rArm.type.is("weap")) {
        RH = rArm;
      } else if (rArm.type.is("shld")) {
        SH = rArm;
      }
    }

    Item lArm = getSlot(Slot.LARM);
    if (lArm != null) {
      if (lArm.type.is("weap")) {
        LH = lArm;
      } else if (lArm.type.is("shld")) {
        SH = lArm;
      }
    }

    if (DEBUG_WCLASS) {
      Gdx.app.debug(TAG, "RH = " + RH);
      Gdx.app.debug(TAG, "LH = " + LH);
      Gdx.app.debug(TAG, "SH = " + SH);
    }

    if (LH != null && RH != null) {
      Weapons.Entry LHEntry = LH.getBase();
      Weapons.Entry RHEntry = RH.getBase();
      if (       LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1hs")) {
        setWeaponClass("1SS"); // Left Swing Right Swing
      } else if (LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1ht")) {
        setWeaponClass("1ST"); // Left Swing Right Thrust
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1hs")) {
        setWeaponClass("1JS"); // Left Jab Right Swing
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1ht")) {
        setWeaponClass("1JT"); // Left Jab Right Thrust
      } else if (LH.type.is("miss") || RH.type.is("miss")) {
        setWeaponClass(LH.type.is("miss") ? LHEntry.wclass : RHEntry.wclass);
      } else if (LH.type.is("h2h")  || RH.type.is("h2h")) {
        setWeaponClass("HT2"); // Two Hand-to-Hand
      } else {
        setWeaponClass("HTH");
        Gdx.app.error(TAG, String.format(
            "Unknown weapon combination: LH=%s RH=%s", LHEntry.wclass, RHEntry.wclass));
      }
    } else if (LH != null || RH != null) {
      RH = ObjectUtils.firstNonNull(RH, LH);
      LH = null;
      if (RH.type.is("bow")) {
        LH = RH;
        RH = null;
        Weapons.Entry LHEntry = LH.getBase();
        setWeaponClass(LHEntry.wclass);
      } else if (RH.type.is("weap")) { // make sure weap and not e.g. misl, might not be required
        Weapons.Entry RHEntry = RH.getBase();
        setWeaponClass(RHEntry.wclass);
      } else {
        setWeaponClass("HTH");
      }
    } else {
      setWeaponClass("HTH");
    }

    setArmType(Component.RH, RH != null ? RH.base.alternateGfx : "");
    setArmType(Component.LH, LH != null ? LH.base.alternateGfx : "");
    setArmType(Component.SH, SH != null ? SH.base.alternateGfx : "");
  }

  private void notifySlotChanged(BodyLoc bodyLoc, Item oldItem, Item item) {
    for (SlotListener l : SLOT_LISTENERS) l.onChanged(this, bodyLoc, oldItem, item);
  }

  private void notifyAlternate(Item LH, Item RH) {
    for (SlotListener l : SLOT_LISTENERS) l.onAlternate(this, LH, RH);
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

  public interface SlotListener {
    void onChanged(Player player, BodyLoc bodyLoc, Item oldItem, Item item);
    void onAlternate(Player player, Item LH, Item RH);
  }

  public static class SlotAdapter implements SlotListener {
    @Override public void onChanged(Player player, BodyLoc bodyLoc, Item oldItem, Item item) {}
    @Override public void onAlternate(Player player, Item LH, Item RH) {}
  }

  public Array<Item> getInventory() {
    return inventory;
  }

  public class Stats {
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
  }
}
