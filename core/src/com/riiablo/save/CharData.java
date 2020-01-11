package com.riiablo.save;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.riiablo.CharacterClass;
import com.riiablo.Riiablo;
import com.riiablo.codec.excel.DifficultyLevels;
import com.riiablo.item.Attributes;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.Location;
import com.riiablo.item.PropertyList;
import com.riiablo.item.Stat;
import com.riiablo.item.StoreLoc;
import com.riiablo.item.Type;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CharData {
  private static final int attack               = 0;
  private static final int kick                 = 1;
  private static final int throw_               = 2;
  private static final int unsummon             = 3;
  private static final int left_hand_throw      = 4;
  private static final int left_hand_swing      = 5;
  private static final int scroll_of_identify   = 217;
  private static final int book_of_identify     = 218;
  private static final int scroll_of_townportal = 219;
  private static final int book_of_townportal   = 220;

  private static final IntIntMap defaultSkills = new IntIntMap();
  static {
    defaultSkills.put(attack, 1);
    defaultSkills.put(kick, 1);
    //defaultSkills.put(throw_, 1);
    defaultSkills.put(unsummon, 1);
    //defaultSkills.put(left_hand_throw, 1);
    defaultSkills.put(left_hand_swing, 1);
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
  final short      questData[][][] = new short[Riiablo.MAX_DIFFS][Riiablo.MAX_ACTS][8];
  final int        waypointData[][] = new int[Riiablo.MAX_DIFFS][Riiablo.MAX_ACTS];
  final long       npcIntroData[] = new long[Riiablo.MAX_DIFFS];
  final long       npcReturnData[] = new long[Riiablo.MAX_DIFFS];
  final Attributes statData = new Attributes();
  final IntIntMap  skillData = new IntIntMap();
  final ItemData   itemData = new ItemData(statData);
        Item       golemItemData;

  public int diff;
  public boolean managed;
  public CharacterClass classId;

  private byte[] data; // TODO: replace this reference with D2S.serialize(CharData)

  final Array<AlternateListener> alternateListeners = new Array<>(false, 16);

  final IntIntMap            skills = new IntIntMap();
  final Array<Stat>          chargedSkills = new Array<>(false, 16);
  final Array<SkillListener> skillListeners = new Array<>(false, 16);

  public static CharData loadFromD2S(int diff, D2S d2s) {
    CharData charData = new CharData(diff, true).load(d2s);
    if (d2s.file != null) charData.data = d2s.file.readBytes();
    return charData;
  }

  public static CharData loadFromBuffer(int diff, ByteBuffer buffer) {
    D2S d2s = D2S.loadFromBuffer(buffer, true);
    return new CharData(diff, false).load(d2s);
  }

  /**
   * @param managed whether or not this data is backed by a file
   */
  private CharData(int diff, boolean managed) {
    this.diff = diff;
    this.managed = managed;
  }

  // created via new character or external player
  public CharData(int diff, boolean managed, String name, byte charClass) {
    this(diff, managed);
    this.name = name;
    this.charClass = charClass;
    classId   = CharacterClass.get(charClass);
    flags     = D2S.FLAG_EXPANSION;
    level     = 1;
    Arrays.fill(hotkeys, D2S.HOTKEY_UNASSIGNED);
    for (int[] actions : actions) Arrays.fill(actions, 0);
    // TODO: check and set town against saved town
    mapSeed   = 0;
  }

  public CharData load(D2S d2s) {
    d2s.copyTo(this);
    preprocessItems();
    return this;
  }

  private void preprocessItems() {
    itemData.preprocessItems();
    mercData.itemData.preprocessItems();
  }

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

    for (int i = 0, i0 = Riiablo.MAX_DIFFS; i < i0; i++) {
      for (int a = 0; a < Riiablo.MAX_ACTS; a++) Arrays.fill(questData[i][a], (short) 0);
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

    alternateListeners.clear();

    skills.clear();
    chargedSkills.clear();
    skillListeners.clear();

    DifficultyLevels.Entry diff = Riiablo.files.DifficultyLevels.get(this.diff);
    PropertyList base = statData.base();
    base.put(Stat.armorclass,      0);
    base.put(Stat.damageresist,    0);
    base.put(Stat.magicresist,     0);
    base.put(Stat.fireresist,      diff.ResistPenalty);
    base.put(Stat.lightresist,     diff.ResistPenalty);
    base.put(Stat.coldresist,      diff.ResistPenalty);
    base.put(Stat.poisonresist,    diff.ResistPenalty);
    base.put(Stat.maxfireresist,   75);
    base.put(Stat.maxlightresist,  75);
    base.put(Stat.maxcoldresist,   75);
    base.put(Stat.maxpoisonresist, 75);
  }

  public boolean isManaged() {
    return managed;
  }

  public byte[] serialize() {
    Validate.isTrue(isManaged(), "Cannot serialize unmanaged data"); // TODO: replace temp check with D2S.serialize(CharData)
    return ArrayUtils.nullToEmpty(data);
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

  public void updateStats() {
//    statData.reset();
//    int[] equippedItems = itemData.equipped.values();
//    for (int i = 0, s = equippedItems.length, j; i < s; i++) {
//      j = equippedItems[i];
//      if (j == ItemData.INVALID_ITEM) continue;
//      Item item = itemData.getItem(j);
//      if (isActive(item)) {
//        statData.add(item.props.remaining());
//        Stat stat;
//        if ((stat = item.props.get(Stat.armorclass)) != null) {
//          statData.aggregate().addCopy(stat);
//        }
//      }
//    }
    IntArray inventoryItems = itemData.getStore(StoreLoc.INVENTORY);
    int[] inventoryItemsCache = inventoryItems.items;
//    for (int i = 0, s = inventoryItems.size, j; i < s; i++) {
//      j = inventoryItemsCache[i];
//      if (j == ItemData.INVALID_ITEM) continue;
//      Item item = itemData.getItem(j);
//      if (item.type.is(Type.CHAR)) {
//        statData.add(item.props.remaining());
//      }
//    }
////    statData.update(this); // TODO: uncomment

    // FIXME: This corrects a mismatch between max and current, algorithm should be tested later for correctness in other cases
    statData.get(Stat.maxstamina).set(statData.get(Stat.stamina));
    statData.get(Stat.maxhp).set(statData.get(Stat.hitpoints));
    statData.get(Stat.maxmana).set(statData.get(Stat.mana));

    // This appears to be hard-coded in the original client
    int dex = statData.get(Stat.dexterity).value();
    Stat armorclass = statData.get(Stat.armorclass);
    armorclass.add(dex / 4);
    armorclass.modified = false;

    skills.clear();
    skills.putAll(skillData);
    skills.putAll(defaultSkills);
    Item LARM = itemData.getEquipped(BodyLoc.LARM);
    Item RARM = itemData.getEquipped(BodyLoc.RARM);
    if ((LARM != null && LARM.typeEntry.Throwable)
     || (RARM != null && RARM.typeEntry.Throwable)) {
      skills.put(throw_, 1);
      if (classId == CharacterClass.BARBARIAN) {
        skills.put(left_hand_throw, 1);
      }
    }
    for (int i = 0, s = inventoryItems.size, j; i < s; i++) {
      j = inventoryItemsCache[i];
      Item item = itemData.getItem(j);
      if (item.type.is(Type.BOOK) || item.type.is(Type.SCRO)) {
        if (item.base.code.equalsIgnoreCase("ibk")) {
          skills.getAndIncrement(book_of_identify, 0, item.props.get(Stat.quantity).value());
        } else if (item.base.code.equalsIgnoreCase("isc")) {
          skills.getAndIncrement(scroll_of_identify, 0, 1);
        } else if (item.base.code.equalsIgnoreCase("tbk")) {
          skills.getAndIncrement(book_of_townportal, 0, item.props.get(Stat.quantity).value());
        } else if (item.base.code.equalsIgnoreCase("tsc")) {
          skills.getAndIncrement(scroll_of_townportal, 0, 1);
        }
      }
    }

    chargedSkills.clear();
    for (Stat stat : statData.remaining()) {
      switch (stat.id) {
        case Stat.item_nonclassskill:
          skills.getAndIncrement(stat.param(), 0, stat.value());
          break;
        case Stat.item_charged_skill:
          chargedSkills.add(stat);
          break;
        default:
          // do nothing
      }
    }
  }

  public int getSkill(int skill) {
    return skills.get(skill, 0);
  }

  public ItemData getItems() {
    return itemData;
  }

  public void itemToCursor(int i) {
    assert itemData.cursor == ItemData.INVALID_ITEM;
    itemData.cursor = i;
    Item item = itemData.getItem(i);
    item.location = Location.CURSOR;
  }

  public void locationToCursor(int i) {
    itemToCursor(i);
  }

  public void cursorToLocation(StoreLoc storeLoc, int x, int y) {
    assert itemData.cursor != ItemData.INVALID_ITEM;
    Item item = itemData.getItem(itemData.cursor);
    item.location = Location.STORED;
    item.storeLoc = storeLoc;
    item.gridX = (byte) x;
    item.gridY = (byte) y;
    itemData.cursor = ItemData.INVALID_ITEM;
  }

  public void swapLocationItem(int i, StoreLoc storeLoc, int x, int y) {
    cursorToLocation(storeLoc, x, y);
    locationToCursor(i);
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

  public void bodyToCursor(BodyLoc bodyLoc, boolean merc) {
    assert itemData.cursor == ItemData.INVALID_ITEM;
    Item item;
    if (merc) {
      int i = mercData.itemData.unequip(bodyLoc);
      itemData.cursor = itemData.add(item = mercData.itemData.remove(i));
    } else {
      itemData.cursor = itemData.unequip(bodyLoc);
      item = itemData.getItem(itemData.cursor);
    }
    item.location = Location.CURSOR;
  }

  public void cursorToBody(BodyLoc bodyLoc, boolean merc) {
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

  public void swapBodyItem(BodyLoc bodyLoc, boolean merc) {
    assert itemData.cursor != ItemData.INVALID_ITEM;
    int oldCursor = itemData.cursor;
    itemData.cursor = ItemData.INVALID_ITEM;
    bodyToCursor(bodyLoc, merc);
    int newCursor = itemData.cursor;
    itemData.cursor = oldCursor;
    cursorToBody(bodyLoc, merc);
    itemData.cursor = newCursor;
  }

  public void beltToCursor(int i) {
    itemToCursor(i);
  }

  public void cursorToBelt(int x, int y) {
    assert itemData.cursor != ItemData.INVALID_ITEM;
    Item item = itemData.getItem(itemData.cursor);
    item.location = Location.BELT;
    item.gridX = (byte) x;
    item.gridY = (byte) y;
    itemData.cursor = ItemData.INVALID_ITEM;
  }

  public void swapBeltItem(int i) {
    assert itemData.cursor != ItemData.INVALID_ITEM;
    int oldCursor = itemData.cursor;
    itemData.cursor = ItemData.INVALID_ITEM;
    beltToCursor(i);
    int newCursor = itemData.cursor;
    itemData.cursor = oldCursor;
    Item oldItem = itemData.getItem(oldCursor);
    cursorToBelt(oldItem.gridX, oldItem.gridY);
    itemData.cursor = newCursor;
  }

  public int getAlternate() {
    return itemData.alternate;
  }

  public void setAlternate(int alternate) {
    if (itemData.alternate != alternate) {
      itemData.alternate = alternate;
      Item LH = itemData.getEquipped(BodyLoc.LARM);
      Item RH = itemData.getEquipped(BodyLoc.RARM);
      updateStats();
      notifyAlternated(alternate, LH, RH);
    }
  }

  public int alternate() {
    int alt = getAlternate() > D2S.PRIMARY ? D2S.PRIMARY : D2S.SECONDARY;
    setAlternate(alt);
    return alt;
  }

  public static class MercData {
    public int   flags;
    public int   seed;
    public short name;
    public short type;
    public int   xp;

    final Attributes statData = new Attributes();
    final ItemData   itemData = new ItemData(statData);

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
    alternateListeners.clear();
    skillListeners.clear();
  }

  public boolean addSkillListener(SkillListener l) {
    skillListeners.add(l);
    return true;
  }

  private void notifySkillChanged(IntIntMap skills, Array<Stat> chargedSkills) {
    for (SkillListener l : skillListeners) l.onChanged(this, skills, chargedSkills);
  }

  public interface SkillListener {
    void onChanged(CharData client, IntIntMap skills, Array<Stat> chargedSkills);
  }

  public boolean addAlternateListener(AlternateListener l) {
    alternateListeners.add(l);
    return true;
  }

  private void notifyAlternated(int alternate, Item LH, Item RH) {
    for (AlternateListener l : alternateListeners) l.onAlternated(this, alternate, LH, RH);
  }

  public interface AlternateListener {
    void onAlternated(CharData charData, int alternate, Item LH, Item RH);
  }
}
