package com.riiablo.save;

import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Pool;

import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.attributes.Attributes;
import com.riiablo.attributes.Stat;
import com.riiablo.attributes.StatListReader;
import com.riiablo.attributes.StatListRef;
import com.riiablo.attributes.StatRef;
import com.riiablo.codec.excel.DifficultyLevels;
import com.riiablo.io.ByteInput;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.ItemReader;
import com.riiablo.item.Location;
import com.riiablo.item.StoreLoc;
import com.riiablo.item.Type;
import com.riiablo.skill.SkillCodes;
import com.riiablo.util.BufferUtils;

// TODO: support pooling CharData for multiplayer
public class CharData implements ItemData.UpdateListener, Pool.Poolable {
  private static final String TAG = "CharData";
  private static final boolean DEBUG       = true;
  private static final boolean DEBUG_ITEMS = DEBUG && !true;

  private static final IntIntMap defaultSkills = new IntIntMap();
  static {
    defaultSkills.put(SkillCodes.attack, 1);
    defaultSkills.put(SkillCodes.kick, 1);
    //defaultSkills.put(SkillCodes.throw_, 1);
    defaultSkills.put(SkillCodes.unsummon, 1);
    //defaultSkills.put(SkillCodes.left_hand_throw, 1);
    defaultSkills.put(SkillCodes.left_hand_swing, 1);
  }

  public       String name;
  public       byte   charClass;
  public       int    flags;
  public       byte   level;
  public final int    hotkeys[] = new int[D2S.NUM_HOTKEYS];
  public final int    actions[][] = new int[D2S.NUM_ACTIONS][D2S.NUM_BUTTONS];
  public final byte   towns[] = new byte[D2S.NUM_DIFFS];
  public       int    mapSeed;
  public final byte   realmData[] = new byte[144];

  final MercData   mercData = new MercData();
  final short      questData[][][] = new short[Riiablo.NUM_DIFFS][Riiablo.NUM_ACTS][8];
  final int        waypointData[][] = new int[Riiablo.NUM_DIFFS][Riiablo.NUM_ACTS];
  final long       npcIntroData[] = new long[Riiablo.NUM_DIFFS];
  final long       npcReturnData[] = new long[Riiablo.NUM_DIFFS];
  final Attributes statData = Attributes.obtainLarge();
  final IntIntMap  skillData = new IntIntMap();
  final ItemData   itemData = new ItemData(statData, null);
        Item       golemItemData;

  public int diff;
  public boolean managed;
  public CharacterClass classId;

  final IntIntMap            skills = new IntIntMap();
  final Array<StatRef>       chargedSkills = new Array<>(false, 16);
  final Array<SkillListener> skillListeners = new Array<>(false, 16);

  @Deprecated
  private static final ItemReader ITEM_READER = new ItemReader(); // TODO: inject
  @Deprecated
  private static final StatListReader STAT_READER = new StatListReader(); // TODO: inject

  /** Constructs a managed instance. Used for local players with complete save data */
  public static CharData loadFromD2S(int diff, D2S d2s) {
    return new CharData().set(diff, true).load(d2s);
  }

  /** Constructs an unmanaged instance. Used for remote players with complete save data. */
  public static CharData loadFromBuffer(int diff, ByteBuffer buffer) {
    byte[] bytes = BufferUtils.readRemaining(buffer);
    ByteInput in = ByteInput.wrap(bytes);
    D2S d2s = D2SReader.INSTANCE.readD2S(in);
    D2SReader.INSTANCE.readRemaining(d2s, in, STAT_READER, ITEM_READER);
    D2SWriterStub.put(d2s, bytes);
    return new CharData().set(diff, false).load(d2s);
  }

  /**
   * @param managed whether or not this data is backed by a file
   */
  public static CharData obtain(int diff, boolean managed, String name, byte charClass) {
    return obtain().set(diff, managed, name, charClass);
  }

  /** Constructs an uninitialized CharData -- must be initialized via #set */
  public static CharData obtain() {
    return new CharData();
  }

  /** Constructs an unmanaged instance. Used for remote players with only partial save data. */
  public static CharData createRemote(String name, byte charClass) {
    return new CharData().set(Riiablo.NORMAL, false, name, charClass);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append(name).append(classId).append("level", level).build();
  }

  public CharData set(int diff, boolean managed) {
    this.diff    = diff;
    this.managed = managed;
    return this;
  }

  public CharData set(int diff, boolean managed, String name, byte charClass) {
    set(diff, managed);
    this.name      = name;
    this.charClass = charClass;
    classId = CharacterClass.get(charClass);
    flags   = D2S.FLAG_EXPANSION;
    level   = 1;
    Arrays.fill(hotkeys, D2S.HOTKEY_UNASSIGNED);
    for (int[] actions : actions) Arrays.fill(actions, 0);
    // TODO: check and set town against saved town
    mapSeed   = 0;
    return this;
  }

  CharData() {}

  public CharData clear() {
    reset();
    return this;
  }

  public CharData load(D2S d2s) {
    /**
     * FIXME: designed to call {@link D2SReader#readRemaining} on local clients
     *        because they will only have had their headers loaded. This is a
     *        problem because network clients already have their remaining data
     *        loaded, and this method shouldn't have access to the remaining
     *        bytes.
     */
    managed = true;
    if (!d2s.bodyRead()) { // FIXME: workaround -- D2GS doesn't have D2S files, but will when authoritative
      byte[] data = D2SWriterStub.getBytes(d2s.name);
      assert data != null : "d2s.bodyRead(" + d2s.bodyRead() + ") but data == null";
      ByteInput in = ByteInput.wrap(data);
      in.skipBytes(D2SReader96.HEADER_SIZE);
      D2SReader.INSTANCE.readRemaining(d2s, in, STAT_READER, ITEM_READER);
    }
    D2SReader.INSTANCE.copyTo(d2s, this);
    preprocessItems();
    itemData.addUpdateListener(this);
    return this;
  }

  private void preprocessItems() {
    itemData.preprocessItems();
    mercData.itemData.preprocessItems();
  }

  @Override
  public void reset() {
    softReset();
    name      = null;
    charClass = -1;
    classId   = null;
    flags     = 0;
    level     = 0;
    Arrays.fill(hotkeys, D2S.HOTKEY_UNASSIGNED);
    for (int i = 0, s = D2S.NUM_ACTIONS; i < s; i++) Arrays.fill(actions[i], 0);
    Arrays.fill(towns, (byte) 0);
    mapSeed   = 0;
    Arrays.fill(realmData, (byte) 0);

    mercData.flags = 0;
    mercData.seed  = 0;
    mercData.name  = 0;
    mercData.type  = 0;
    mercData.xp    = 0;

    for (int i = 0, i0 = Riiablo.NUM_DIFFS; i < i0; i++) {
      for (int a = 0; a < Riiablo.NUM_ACTS; a++) Arrays.fill(questData[i][a], (short) 0);
      Arrays.fill(waypointData[i], 0);
      npcIntroData[i] = 0;
      npcReturnData[i] = 0;
    }
  }

  void softReset() {
    statData.base().clear();
    statData.reset();
    skillData.clear();
    itemData.clear();
    mercData.statData.base().clear();
    mercData.statData.reset();
    mercData.itemData.clear();
    golemItemData = null;

    skills.clear();
    chargedSkills.clear();
    skillListeners.clear();

    DifficultyLevels.Entry diff = Riiablo.files.DifficultyLevels.get(this.diff);
    StatListRef base = statData.base();
    base.put(Stat.strength, 0);
    base.put(Stat.energy, 0);
    base.put(Stat.dexterity, 0);
    base.put(Stat.vitality, 0);
    base.put(Stat.statpts, 0);
    base.put(Stat.newskills, 0);
    base.put(Stat.hitpoints, 0);
    base.put(Stat.maxhp, 0);
    base.put(Stat.mana, 0);
    base.put(Stat.maxmana, 0);
    base.put(Stat.stamina, 0);
    base.put(Stat.maxstamina, 0);
    base.put(Stat.level, 0);
    base.put(Stat.experience, 0);
    base.put(Stat.gold, 0);
    base.put(Stat.goldbank, 0);
    base.put(Stat.armorclass, 0);
    base.put(Stat.damageresist, 0);
    base.put(Stat.magicresist, 0);
    base.put(Stat.fireresist, diff.ResistPenalty);
    base.put(Stat.lightresist, diff.ResistPenalty);
    base.put(Stat.coldresist, diff.ResistPenalty);
    base.put(Stat.poisonresist, diff.ResistPenalty);
    base.put(Stat.maxfireresist, 75);
    base.put(Stat.maxlightresist, 75);
    base.put(Stat.maxcoldresist, 75);
    base.put(Stat.maxpoisonresist, 75);

    // TODO: set base merc stats based on hireling tables and level
    base = mercData.statData.base();
    base.put(Stat.strength, 0);
    base.put(Stat.energy, 0);
    base.put(Stat.dexterity, 0);
    base.put(Stat.vitality, 0);
    base.put(Stat.statpts, 0);
    base.put(Stat.newskills, 0);
    base.put(Stat.hitpoints, 0);
    base.put(Stat.maxhp, 0);
    base.put(Stat.mana, 0);
    base.put(Stat.maxmana, 0);
    base.put(Stat.stamina, 0);
    base.put(Stat.maxstamina, 0);
    base.put(Stat.level, 0);
    base.put(Stat.experience, 0);
    base.put(Stat.gold, 0);
    base.put(Stat.goldbank, 0);
    base.put(Stat.armorclass, 0);
    base.put(Stat.damageresist, 0);
    base.put(Stat.magicresist, 0);
    base.put(Stat.fireresist, diff.ResistPenalty);
    base.put(Stat.lightresist, diff.ResistPenalty);
    base.put(Stat.coldresist, diff.ResistPenalty);
    base.put(Stat.poisonresist, diff.ResistPenalty);
    base.put(Stat.maxfireresist, 75);
    base.put(Stat.maxlightresist, 75);
    base.put(Stat.maxcoldresist, 75);
    base.put(Stat.maxpoisonresist, 75);
  }

  public void preloadItems() {
    itemData.load();
    mercData.itemData.load();
  }

  public boolean isManaged() {
    return managed;
  }

  public byte[] serialize() {
    /** TODO: replace this code when {@link D2SWriter} is implemented */
    Validate.isTrue(isManaged(), "Cannot serialize unmanaged data");
    return D2SWriterStub.getBytes(name);
  }

  public int getHotkey(int button, int skill) {
    return ArrayUtils.indexOf(hotkeys, button == Input.Buttons.LEFT ? skill | D2S.HOTKEY_LEFT_MASK : skill);
  }

  public void setHotkey(int button, int skill, int index) {
    hotkeys[index] = button == Input.Buttons.LEFT ? skill | D2S.HOTKEY_LEFT_MASK : skill;
  }

  public int getAction(int button) {
    return getAction(itemData.alternate, button);
  }

  public int getAction(int alternate, int button) {
    return actions[alternate][button];
  }

  public void setAction(int button, int skill) {
    setAction(itemData.alternate, button, skill);
  }

  public void setAction(int alternate, int button, int skill) {
    actions[alternate][button] = skill;
  }

  public boolean hasMerc() {
    return mercData.seed != 0;
  }

  public MercData getMerc() {
    return mercData;
  }

  public short[] getQuests(int act) {
    return questData[diff][act];
  }

  public int getWaypoints(int act) {
    return waypointData[diff][act];
  }

  public long getNpcIntro() {
    return npcIntroData[diff];
  }

  public long getNpcReturn() {
    return npcReturnData[diff];
  }

  public boolean hasGolemItem() {
    return golemItemData != null;
  }

  public Item getGolemItem() {
    return golemItemData;
  }

  public Attributes getStats() {
    return statData;
  }

  public void update() {
    onUpdated(itemData);
  }

  @Override
  public void onUpdated(ItemData itemData) {
    assert itemData.stats == statData;

    // FIXME: This corrects a mismatch between max and current, algorithm should be tested later for correctness in other cases
    statData.get(Stat.maxstamina).set(statData.get(Stat.stamina));
    statData.get(Stat.maxhp).set(statData.get(Stat.hitpoints));
    statData.get(Stat.maxmana).set(statData.get(Stat.mana));

    // This appears to be hard-coded in the original client
    int dex = statData.get(Stat.dexterity).asInt();
    StatRef armorclass = statData.get(Stat.armorclass);
    armorclass.add(dex / 4);
    armorclass.forceUnmodified();

    skills.clear();
    skills.putAll(skillData);
    skills.putAll(defaultSkills);
    Item LARM = itemData.getEquipped(BodyLoc.LARM);
    Item RARM = itemData.getEquipped(BodyLoc.RARM);
    if ((LARM != null && LARM.typeEntry.Throwable)
     || (RARM != null && RARM.typeEntry.Throwable)) {
      skills.put(SkillCodes.throw_, 1);
      if (classId == CharacterClass.BARBARIAN) {
        skills.put(SkillCodes.left_hand_throw, 1);
      }
    }
    IntArray inventoryItems = itemData.getStore(StoreLoc.INVENTORY);
    int[] cache = inventoryItems.items;
    for (int i = 0, s = inventoryItems.size, j; i < s; i++) {
      j = cache[i];
      Item item = itemData.getItem(j);
      if (item.type.is(Type.BOOK) || item.type.is(Type.SCRO)) {
        if (item.base.code.equalsIgnoreCase("ibk")) {
          skills.getAndIncrement(SkillCodes.book_of_identify, 0, item.attrs.get(Stat.quantity).asInt());
        } else if (item.base.code.equalsIgnoreCase("isc")) {
          skills.getAndIncrement(SkillCodes.scroll_of_identify, 0, 1);
        } else if (item.base.code.equalsIgnoreCase("tbk")) {
          skills.getAndIncrement(SkillCodes.book_of_townportal, 0, item.attrs.get(Stat.quantity).asInt());
        } else if (item.base.code.equalsIgnoreCase("tsc")) {
          skills.getAndIncrement(SkillCodes.scroll_of_townportal, 0, 1);
        }
      }
    }

    chargedSkills.clear();
    for (StatRef stat : statData.remaining()) {
      switch (stat.id()) {
        case Stat.item_nonclassskill:
          skills.getAndIncrement(stat.encodedParams(), 0, stat.asInt());
          break;
        case Stat.item_charged_skill:
          chargedSkills.add(stat.copy());
          break;
        default:
          // do nothing
      }
    }
    notifySkillChanged(skills, chargedSkills);
  }

  public int getSkill(int skill) {
    return skills.get(skill, 0);
  }

  public ItemData getItems() {
    return itemData;
  }

//  @Override
  public void groundToCursor(Item item) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "groundToCursor " + item);
    itemData.pickup(item);
  }

//  @Override
  public void cursorToGround() {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "cursorToGround");
    itemData.drop();
  }

  public void itemToCursor(int i) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "itemToCursor " + i);
    itemData.pickup(i);
  }

//  @Override
  public void storeToCursor(int i) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "storeToCursor " + i);
    itemToCursor(i);
  }

//  @Override
  public void cursorToStore(StoreLoc storeLoc, int x, int y) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "cursorToStore " + storeLoc + "," + x + "," + y);
    itemData.storeCursor(storeLoc, x, y);
  }

//  @Override
  public void swapStoreItem(int i, StoreLoc storeLoc, int x, int y) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "swapStoreItem " + i + "," + storeLoc + "," + x + "," + y);
    cursorToStore(storeLoc, x, y);
    storeToCursor(i);
  }

  public void bodyToCursor(BodyLoc bodyLoc) {
    bodyToCursor(bodyLoc, false);
  }

  public void cursorToBody(BodyLoc bodyLoc) {
    cursorToBody(bodyLoc, false);
  }

  public void swapBodyItem(BodyLoc bodyLoc) {
    swapBodyItem(bodyLoc, false);
  }

//  @Override
  public void bodyToCursor(BodyLoc bodyLoc, boolean merc) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "bodyToCursor " + bodyLoc + "," + (merc ? "merc" : "player"));
    assert itemData.cursor == ItemData.INVALID_ITEM;
    Item item;
    if (merc) {
      int i = mercData.itemData.unequip(bodyLoc);
      itemData.cursor = itemData.add(item = mercData.itemData.remove(i));
    } else {
      itemData.cursor = itemData.unequip(bodyLoc);
      item = itemData.getItem(itemData.cursor);
    }
    itemData.setLocation(item, Location.CURSOR);
  }

//  @Override
  public void cursorToBody(BodyLoc bodyLoc, boolean merc) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "cursorToBody " + bodyLoc + "," + (merc ? "merc" : "player"));
    assert itemData.cursor != ItemData.INVALID_ITEM;
    if (merc) {
      Item item = itemData.getItem(itemData.cursor);
      itemData.remove(itemData.cursor);
      mercData.itemData.equip(bodyLoc, item);
    } else {
      itemData.equip(bodyLoc, itemData.cursor);
    }
    itemData.cursor = ItemData.INVALID_ITEM;
  }

  /**
   * FIXME: originally worked as an aggregate call on {@link #cursorToBody(BodyLoc, boolean)} and
   *        {@link #bodyToCursor(BodyLoc, boolean)}, and while that worked fine programically to
   *        pass the assertions within {@link ItemData}, {@link ItemData.LocationListener#onChanged}
   *        was being called out of order for setting the cursor, causing the cursor to be unset
   *        within the UI immediately after being changed.
   */
//  @Override
  public void swapBodyItem(BodyLoc bodyLoc, boolean merc) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "swapBodyItem " + bodyLoc + "," + (merc ? "merc" : "player"));

    // #bodyToCursor(BodyLoc,boolean)
    Item newCursorItem;
    int newCursor;
    if (merc) {
      int i = mercData.itemData.unequip(bodyLoc);
      newCursor = itemData.add(newCursorItem = mercData.itemData.remove(i));
    } else {
      newCursor = itemData.unequip(bodyLoc);
      newCursorItem = itemData.getItem(newCursor);
    }

    // #cursorToBody(BodyLoc,boolean)
    if (merc) {
      Item item = itemData.getItem(itemData.cursor);
      itemData.remove(itemData.cursor);
      mercData.itemData.equip(bodyLoc, item);
      if (newCursor >= itemData.cursor) newCursor--; // removing item invalidated the index
    } else {
      itemData.equip(bodyLoc, itemData.cursor);
    }

    itemData.cursor = newCursor;
    itemData.setLocation(newCursorItem, Location.CURSOR);
  }

//  @Override
  public void beltToCursor(int i) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "beltToCursor");
    itemToCursor(i);
  }

//  @Override
  public void cursorToBelt(int x, int y) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "cursorToBelt");
    assert itemData.cursor != ItemData.INVALID_ITEM;
    int i = itemData.cursor;
    itemData.cursor = ItemData.INVALID_ITEM;
    Item item = itemData.getItem(i);
    item.gridX = (byte) x;
    item.gridY = (byte) y;
    itemData.setLocation(item, Location.BELT);
  }

  /**
   * FIXME: originally worked as an aggregate call on {@link #cursorToBelt(int, int)} and
   *        {@link #beltToCursor(int)}, and while that worked fine programically to pass the
   *        assertions within {@link ItemData}, {@link ItemData.LocationListener#onChanged}
   *        was being called out of order for setting the cursor, causing the cursor to be unset
   *        within the UI immediately after being changed.
   */
//  @Override
  public void swapBeltItem(int i) {
    if (DEBUG_ITEMS) Gdx.app.log(TAG, "swapBeltItem");

    // #beltToCursor(int)
    Item newCursorItem = itemData.getItem(i);

    // #cursorToBelt(int,int)
    Item item = itemData.getItem(itemData.cursor);
    item.gridX = newCursorItem.gridX;
    item.gridY = newCursorItem.gridY;
    itemData.setLocation(item, Location.BELT);
    itemData.cursor = ItemData.INVALID_ITEM;

    itemData.pickup(i);
  }

  public static class MercData {
    public int   flags;
    public int   seed;
    public short name;
    public short type;
    public long  xp;

    final Attributes statData = Attributes.obtainLarge();
    final ItemData   itemData = new ItemData(statData, null);

    public Attributes getStats() {
      return statData;
    }

    public ItemData getItems() {
      return itemData;
    }

    public String getName() {
      return String.format("0x%04X", name);
    }
  }

  public void clearListeners() {
    itemData.equipListeners.clear();
    mercData.itemData.equipListeners.clear();
    itemData.alternateListeners.clear();
    mercData.itemData.alternateListeners.clear();
    skillListeners.clear();
  }

  public boolean addSkillListener(SkillListener l) {
    skillListeners.add(l);
    return true;
  }

  private void notifySkillChanged(IntIntMap skills, Array<StatRef> chargedSkills) {
    for (SkillListener l : skillListeners) l.onChanged(this, skills, chargedSkills);
  }

  public interface SkillListener {
    void onChanged(CharData client, IntIntMap skills, Array<StatRef> chargedSkills);
  }
}
