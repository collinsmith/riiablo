package com.riiablo.entity;

import com.google.common.base.Preconditions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.COF;
import com.riiablo.codec.D2S;
import com.riiablo.codec.excel.Armor;
import com.riiablo.codec.excel.Weapons;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.map.DT1.Tile;
import com.riiablo.map.Map;
import com.riiablo.server.Connect;

import org.apache.commons.lang3.ObjectUtils;

import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class Player extends Entity {
  private static final String TAG = "Player";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_STATE = DEBUG && !true;
  private static final boolean DEBUG_INV   = DEBUG && !true;
  private static final boolean DEBUG_EQUIP = DEBUG && !true;

  public static final int MAX_NAME_LENGTH = 15;

  public static final byte MODE_DT =  0;
  public static final byte MODE_NU =  1;
  public static final byte MODE_WL =  2;
  public static final byte MODE_RN =  3;
  public static final byte MODE_GH =  4;
  public static final byte MODE_TN =  5;
  public static final byte MODE_TW =  6;
  public static final byte MODE_A1 =  7;
  public static final byte MODE_A2 =  8;
  public static final byte MODE_BL =  9;
  public static final byte MODE_SC = 10;
  public static final byte MODE_TH = 11;
  public static final byte MODE_KK = 12;
  public static final byte MODE_S1 = 13;
  public static final byte MODE_S2 = 14;
  public static final byte MODE_S3 = 15;
  public static final byte MODE_S4 = 16;
  public static final byte MODE_DD = 17;
  //public static final byte MODE_GH = 18;
  //public static final byte MODE_GH = 19;

  private static final int MOVING_MODES = (1 << MODE_WL) | (1 << MODE_RN) | (1 << MODE_TW);
  private static final int CASTING_MODES = (1 << MODE_SC);
  private static final int CASTABLE_MODES = (1 << MODE_NU) | (1 << MODE_TN) | MOVING_MODES;

  private static final String[] TOKENS = {"AM", "SO", "NE", "PA", "BA", "DZ", "AI"};

  public static String getToken(int type) {
    return TOKENS[type];
  }

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

  boolean alternate;
  boolean ignoreUpdate;
  public Stats stats;
  public Skills skills;
  public int[] skillBar;
  public Map map;
  public Map.Zone curZone;
  public final CharacterClass charClass;

  Array<Item> inventory = new Array<>();

  EnumMap<BodyLoc, Item> equipped = new EnumMap<>(BodyLoc.class);
  final Set<SlotListener> SLOT_LISTENERS = new CopyOnWriteArraySet<>();

  public Player(String name, CharacterClass characterClass) {
    this(name, characterClass.id);
    stats = new StatsImpl(name, characterClass.id);
    skills = new SkillsImpl();
    skillBar = new int[16];
  }

  public Player(D2S d2s) {
    this(d2s.name, d2s.charClass);
    stats = new D2SStats(d2s);
    skills = new D2SSkills(d2s);
    skillBar = d2s.skillBar;
    loadEquipped(d2s.items.equipped);
    loadInventory(d2s.items.inventory);
  }

  public Player(Connect connect) {
    this(connect.name, connect.classId);

    ignoreUpdate = true;
    setWeapon(WEAPON_1HS);
    setComponents(connect.composites);
    setTransforms(connect.colors);
  }

  Player(String name, int classId) {
    super(Type.PLR, "player", TOKENS[classId]);
    charClass = CharacterClass.get(classId);
    setMode(MODE_TN);
    setWalkSpeed(6);
    setRunSpeed(9);
    setRunning(true);
    angle(-MathUtils.PI / 2);
  }

  private void loadEquipped(EnumMap<BodyLoc, Item> items) {
    equipped.putAll(items);
    for (java.util.Map.Entry<BodyLoc, Item> entry : items.entrySet()) {
      entry.getValue().load();
      if (DEBUG_EQUIP) Gdx.app.debug(TAG, entry.getKey() + ": " + entry.getValue());
    }
  }

  private void loadInventory(Array<Item> items) {
    inventory.addAll(items);
    for (Item item : items) {
      item.load();
      if (DEBUG_INV) Gdx.app.debug(TAG, item.gridX + "," + item.gridY + ": " + item);
    }
  }

  public Array<Item> getInventory() {
    return inventory;
  }

  @Override
  public byte getNeutralMode() {
    return (curZone != null && curZone.isTown()) ? MODE_TN : MODE_NU;
  }

  @Override
  public byte getWalkMode() {
    return (curZone != null && curZone.isTown()) ? MODE_TW : MODE_WL;
  }

  @Override
  public byte getRunMode() {
    return MODE_RN;
  }

  boolean ignoreFootstep = false;

  @Override
  public void act(float delta) {
    super.act(delta);
    if (animation != null && (mode == getWalkMode() || mode == getRunMode())) {
      int frame = animation.getFrame();
      int numFrames = animation.getNumFramesPerDir();
      if (frame == 0 || frame == numFrames >>> 1) {
        if (ignoreFootstep) return;
        ignoreFootstep = true;
        int x = Map.round(position.x);
        int y = Map.round(position.y);
        int tx = x < 0
            ? ((x + 1) / Tile.SUBTILE_SIZE) - 1
            : (x / Tile.SUBTILE_SIZE);
        int ty = y < 0
            ? ((y + 1) / Tile.SUBTILE_SIZE) - 1
            : (y / Tile.SUBTILE_SIZE);
        Map map = Map.instance;
        Map.Zone zone = map.getZone(x, y);
        Tile tile = map.getTile(0, tx, ty);
        String type = DT1Sound.getType(zone.level, tile);
        Riiablo.audio.play("light_run_" + type + "_1", true);
      } else {
        ignoreFootstep = false;
      }
    }
  }

  @Override
  protected void updateCOF() {
    if (ignoreUpdate) {
      super.updateCOF();
      return;
    }

    updateWeaponClass();

    Item head = getSlot(Slot.HEAD);
    setComponent(COF.Component.HD, head != null ? (byte) Type.PLR.getComponent(head.base.alternateGfx) : (byte) 1);
    setTransform(COF.Component.HD, head != null ? (byte) ((head.base.Transform << 5) | (head.charColorIndex & 0x1F)) : (byte) 0xFF);

    Item body = getSlot(Slot.TORS);
    if (body != null) {
      Armor.Entry armor = body.getBase();
      setComponent(COF.Component.TR, (byte) (armor.Torso + 1));
      setComponent(COF.Component.LG, (byte) (armor.Legs  + 1));
      setComponent(COF.Component.RA, (byte) (armor.rArm  + 1));
      setComponent(COF.Component.LA, (byte) (armor.lArm  + 1));
      setComponent(COF.Component.S1, (byte) (armor.lSPad + 1));
      setComponent(COF.Component.S2, (byte) (armor.rSPad + 1));

      byte packedTransform = (byte) ((body.base.Transform << 5) | (body.charColorIndex & 0x1F));
      setTransform(COF.Component.TR, packedTransform);
      setTransform(COF.Component.LG, packedTransform);
      setTransform(COF.Component.RA, packedTransform);
      setTransform(COF.Component.LA, packedTransform);
      setTransform(COF.Component.S1, packedTransform);
      setTransform(COF.Component.S2, packedTransform);
    } else {
      setComponent(COF.Component.TR, (byte) 1);
      setComponent(COF.Component.LG, (byte) 1);
      setComponent(COF.Component.RA, (byte) 1);
      setComponent(COF.Component.LA, (byte) 1);
      setComponent(COF.Component.S1, (byte) 1);
      setComponent(COF.Component.S2, (byte) 1);

      setTransform(COF.Component.TR, (byte) 0xFF);
      setTransform(COF.Component.LG, (byte) 0xFF);
      setTransform(COF.Component.RA, (byte) 0xFF);
      setTransform(COF.Component.LA, (byte) 0xFF);
      setTransform(COF.Component.S1, (byte) 0xFF);
      setTransform(COF.Component.S2, (byte) 0xFF);
    }

    super.updateCOF();
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

    if (DEBUG_STATE) {
      Gdx.app.debug(TAG, "RH = " + RH);
      Gdx.app.debug(TAG, "LH = " + LH);
      Gdx.app.debug(TAG, "SH = " + SH);
    }

    if (LH != null && RH != null) {
      Weapons.Entry LHEntry = LH.getBase();
      Weapons.Entry RHEntry = RH.getBase();
      if (       LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1hs")) {
        setWeapon(WEAPON_1SS); // Left Swing Right Swing
      } else if (LHEntry.wclass.equals("1hs") && RHEntry.wclass.equals("1ht")) {
        setWeapon(WEAPON_1ST); // Left Swing Right Thrust
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1hs")) {
        setWeapon(WEAPON_1JS); // Left Jab Right Swing
      } else if (LHEntry.wclass.equals("1ht") && RHEntry.wclass.equals("1ht")) {
        setWeapon(WEAPON_1JT); // Left Jab Right Thrust
      } else if (LH.type.is("miss") || RH.type.is("miss")) {
        setWeapon((byte) Riiablo.files.WeaponClass.index(LH.type.is("miss") ? LHEntry.wclass : RHEntry.wclass));
      } else if (LH.type.is("h2h")  || RH.type.is("h2h")) {
        setWeapon(WEAPON_HT2); // Two Hand-to-Hand
      } else {
        setWeapon(WEAPON_HTH);
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
        setWeapon((byte) Riiablo.files.WeaponClass.index(LHEntry.wclass));
      } else if (RH.type.is("weap")) { // make sure weap and not e.g. misl, might not be required
        Weapons.Entry RHEntry = RH.getBase();
        setWeapon((byte) Riiablo.files.WeaponClass.index(RHEntry.wclass));
      } else {
        setWeapon(WEAPON_HTH);
      }
    } else {
      setWeapon(WEAPON_HTH);
    }

    setComponent(COF.Component.RH, RH != null ? (byte) Type.PLR.getComponent(RH.base.alternateGfx) : 0);
    setComponent(COF.Component.LH, LH != null ? (byte) Type.PLR.getComponent(LH.base.alternateGfx) : 0);
    setComponent(COF.Component.SH, SH != null ? (byte) Type.PLR.getComponent(SH.base.alternateGfx) : 0);
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

    //setTransform();
    int components = loc.components();
    invalidate(components);
    updateWeaponClass();

    notifySlotChanged(loc, oldItem, item);
    return oldItem;
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
  public boolean isCasting(byte mode) {
    return (CASTING_MODES & (1 << mode)) != 0;
  }

  @Override
  public boolean isCastable(byte mode) {
    return (CASTABLE_MODES & (1 << mode)) != 0;
  }

  @Override
  public boolean isMoving(byte mode) {
    return (MOVING_MODES & (1 << mode)) != 0;
  }

  private void notifySlotChanged(BodyLoc bodyLoc, Item oldItem, Item item) {
    for (SlotListener l : SLOT_LISTENERS) l.onChanged(this, bodyLoc, oldItem, item);
  }

  private void notifyAlternate(Item LH, Item RH) {
    for (SlotListener l : SLOT_LISTENERS) l.onAlternate(this, LH, RH);
  }

  public boolean addSlotListener(SlotListener l) {
    return SLOT_LISTENERS.add(l);
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

  public interface Stats {
    int getClassId();
    CharacterClass getCharClass();
    String getName();
    int getLevel();
    long getExperience();
    int getStrength();
    int getDexterity();
    int getVitality();
    int getEnergy();
    int getFireResistance();
    int getColdResistance();
    int getLightningResistance();
    int getPoisonResistance();
  }

  public class StatsImpl implements Stats {
    final String name;
    final int    classId;
    StatsImpl(String name, int classId) {
      this.name = name;
      this.classId = classId;
    }

    @Override
    public int getClassId() {
      return classId;
    }

    @Override
    public CharacterClass getCharClass() {
      return CharacterClass.get(getClassId());
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public int getLevel() {
      return 0;
    }

    @Override
    public long getExperience() {
      return 0;
    }

    @Override
    public int getStrength() {
      return 0;
    }

    @Override
    public int getDexterity() {
      return 0;
    }

    @Override
    public int getVitality() {
      return 0;
    }

    @Override
    public int getEnergy() {
      return 0;
    }

    @Override
    public int getFireResistance() {
      return 0;
    }

    @Override
    public int getColdResistance() {
      return 0;
    }

    @Override
    public int getLightningResistance() {
      return 0;
    }

    @Override
    public int getPoisonResistance() {
      return 0;
    }
  }

  public class D2SStats implements Stats {
    public final D2S d2s;
    D2SStats(D2S d2s) {
      this.d2s = d2s;
    }

    @Override
    public int getClassId() {
      return d2s.charClass;
    }

    @Override
    public CharacterClass getCharClass() {
      return CharacterClass.get(getClassId());
    }

    @Override
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

  public interface Skills {
    int getLevel(int skill);
  }

  public class SkillsImpl implements Skills {
    SkillsImpl() {}

    @Override
    public int getLevel(int skill) {
      return 0;
    }
  }

  public class D2SSkills implements Skills {
    public final D2S.SkillData skills;
    D2SSkills(D2S d2s) {
      this.skills = d2s.skills;
    }

    @Override
    public int getLevel(int skill) {
      skill -= charClass.firstSpell;
      return skills.data[skill];
    }
  }
}
