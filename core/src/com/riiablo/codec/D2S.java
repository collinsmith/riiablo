package com.riiablo.codec;

import com.google.common.primitives.Ints;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.riiablo.Riiablo;
import com.riiablo.engine.Engine;
import com.riiablo.codec.util.BitStream;
import com.riiablo.item.Item;
import com.riiablo.item.Location;
import com.riiablo.util.BufferUtils;
import com.riiablo.util.DebugUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;

public class D2S {
  private static final String TAG = "D2S";
  private static final boolean DEBUG           = !true;
  private static final boolean DEBUG_HEADER    = DEBUG && true;
  private static final boolean DEBUG_QUESTS    = DEBUG && true;
  private static final boolean DEBUG_WAYPOINTS = DEBUG && true;
  private static final boolean DEBUG_NPCS      = DEBUG && true;
  private static final boolean DEBUG_STATS     = DEBUG && true;
  private static final boolean DEBUG_SKILLS    = DEBUG && true;
  private static final boolean DEBUG_ITEMS     = DEBUG && true;
  private static final boolean DEBUG_GOLEM     = DEBUG && true;

  public static final String EXT = "d2s";

  static final int MAGIC_NUMBER = 0xAA55AA55;

  static final int VERSION_100 = 71;
  static final int VERSION_107 = 87;
  static final int VERSION_108 = 89;
  static final int VERSION_109 = 92;
  static final int VERSION_110 = 96;

  public static final int FLAG_BIT0      = 1 << 0;
  public static final int FLAG_BIT1      = 1 << 1;
  public static final int FLAG_HARDCORE  = 1 << 2;
  public static final int FLAG_DIED      = 1 << 3;
  public static final int FLAG_BIT4      = 1 << 4;
  public static final int FLAG_EXPANSION = 1 << 5;
  public static final int FLAG_BIT6      = 1 << 6;
  public static final int FLAG_BIT7      = 1 << 7;

  public static final int HOTKEY_UNASSIGNED = 0xFFFF;
  public static final int HOTKEY_LEFT_MASK  = 0x8000;

  public static final int PRIMARY     = 0;
  public static final int SECONDARY   = 1;
  public static final int NUM_ALTS    = 2;
  public static final int NUM_ACTIONS = NUM_ALTS;
  public static final int NUM_BUTTONS = 2;
  public static final int NUM_HOTKEYS = 16;
  public static final int NUM_DIFFS   = 3;  // TODO: Point at Diablo.MAX_DIFFICULTIES or something

  static final int DIFF_ACT_MASK    = 0x7;
  static final int DIFF_FLAG_ACTIVE = 1 << 7;

  public final FileHandle file;
  public final Header     header;

  public QuestData    quests;
  public WaypointData waypoints;
  public NPCData      npcs;
  public StatData     stats;
  public SkillData    skills;
  public ItemData     items;
  public GolemData    golem;

  public D2S(FileHandle file, Header header) {
    this.file = file;
    this.header = header;
  }

  public void loadRemaining() {
    if (file == null) return;
    ByteBuffer buffer = file.map().order(ByteOrder.LITTLE_ENDIAN);
    buffer.position(Header.SIZE);
    quests    = QuestData.obtain(buffer);
    waypoints = WaypointData.obtain(buffer);
    npcs      = NPCData.obtain(buffer);
    stats     = StatData.obtain(BufferUtils.slice(buffer, SkillData.SECTION_HEADER));
    skills    = SkillData.obtain(buffer);
    items     = ItemData.obtain(buffer, ItemData.SECTION_FOOTER, true);
    header.merc.items = MercData.MercItemData.obtain(header.merc, buffer);
    golem     = GolemData.obtain(buffer);
    if (DEBUG_QUESTS)    Gdx.app.debug(TAG, quests.toString());
    if (DEBUG_WAYPOINTS) Gdx.app.debug(TAG, waypoints.toString());
    if (DEBUG_NPCS)      Gdx.app.debug(TAG, npcs.toString());
    if (DEBUG_STATS)     Gdx.app.debug(TAG, stats.toString());
    if (DEBUG_SKILLS)    Gdx.app.debug(TAG, skills.toString());
    if (DEBUG_ITEMS) {
      Gdx.app.debug(TAG, items.toString());
      for (Item item : items.items) {
        Gdx.app.debug(TAG, item.toString());
      }

      Gdx.app.debug(TAG, header.merc.items.toString());
      for (Item item : header.merc.items.items.items) {
        Gdx.app.debug(TAG, item.toString());
      }
    }
    if (DEBUG_GOLEM) Gdx.app.debug(TAG, golem.toString());
    assert !buffer.hasRemaining();
  }

  public static D2S loadFromFile(FileHandle file) {
    ByteBuffer buffer = file.map().order(ByteOrder.LITTLE_ENDIAN);
    Header header = Header.obtain(buffer);
    if (DEBUG_HEADER) Gdx.app.debug(TAG, header.toString());
    if (header.magicNumber != MAGIC_NUMBER) throw new GdxRuntimeException("Magic number doesn't match " + String.format("0x%08X", MAGIC_NUMBER) + ": " + String.format("0x%08X", header.magicNumber));
    if (header.version != VERSION_110) throw new GdxRuntimeException("Unsupported D2S version: " + header.version + " -- Only supports " + header.getVersionString(VERSION_110));
    if (header.size != file.length()) Gdx.app.error(TAG, "Save file size doesn't match encoded size for character " + header.name + ". Should be: " + header.size);
    return new D2S(file, header);
  }

  public static class Header {
    static final int SIZE = 0x14F;

    public int      magicNumber;
    public int      version;
    public int      size;
    public int      checksum;
    public int      alternate;
    public String   name;
    public int      flags;
    public byte     charClass;
    public byte     unk1[];
    public byte     level;
    public byte     unk2[];
    public int      timestamp;
    public byte     unk3[];
    public int      hotkeys[];
    public int      actions[][];
    public byte     composites[];
    public byte     colors[];
    public byte     towns[];
    public int      mapSeed;
    public MercData merc;
    public byte     realmData[];

    static Header obtain(ByteBuffer buffer) {
      return new Header().read(buffer);
    }

    Header read(ByteBuffer buffer) {
      ByteBuffer slice = BufferUtils.slice(buffer, SIZE);
      magicNumber   = slice.getInt();
      version       = slice.getInt();
      size          = slice.getInt();
      checksum      = slice.getInt();
      alternate     = slice.getInt();
      name          = BufferUtils.readString2(slice, Engine.MAX_NAME_LENGTH + 1);
      flags         = slice.getInt();
      charClass     = slice.get();
      unk1          = BufferUtils.readBytes(slice, 2);
      level         = slice.get();
      unk2          = BufferUtils.readBytes(slice, Ints.BYTES);
      timestamp     = slice.getInt();
      unk3          = BufferUtils.readBytes(slice, Ints.BYTES);
      hotkeys       = BufferUtils.readInts(slice, NUM_HOTKEYS);
      actions       = new int[NUM_ACTIONS][NUM_BUTTONS];
      for (int i = 0; i < NUM_ACTIONS; i++) actions[i] = BufferUtils.readInts(slice, NUM_BUTTONS);
      composites    = BufferUtils.readBytes(slice, com.riiablo.codec.COF.Component.NUM_COMPONENTS);
      colors        = BufferUtils.readBytes(slice, COF.Component.NUM_COMPONENTS);
      towns         = BufferUtils.readBytes(slice, NUM_DIFFS);
      mapSeed       = slice.getInt();
      merc          = MercData.obtain(slice);
      realmData     = BufferUtils.readBytes(slice, 144);
      assert !slice.hasRemaining();
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("name", name)
          .append("level", level)
          .append("title", getProgressionString())
          .append("class", getClassName())
          //.append("magicNumber", String.format("0x%08X", magicNumber))
          .append("version", getVersionString())
          .append("size", size + "B")
          .append("checksum", String.format("0x%08X", checksum))
          .append("flags", getFlagsString())
          .append("unk1", DebugUtils.toByteArray(unk1))
          .append("unk2", DebugUtils.toByteArray(unk2))
          .append("timestamp", new Date(timestamp * 1000L).toString())
          .append("unk3", DebugUtils.toByteArray(unk3))
          .append("hotkeys", Arrays.toString(hotkeys))
          .append("actions[PRIMARY]", Arrays.toString(actions[PRIMARY]))
          .append("actions[SECONDARY]", Arrays.toString(actions[SECONDARY]))
          .append("composites", DebugUtils.toByteArray(composites))
          .append("colors", DebugUtils.toByteArray(colors))
          .append("towns", getTownsString())
          .append("mapSeed", String.format("0x%08X", mapSeed))
          .append("merc", merc)
          .build();
    }

    public static String getVersionString(int versionCode) {
      switch (versionCode) {
        case VERSION_100: return "1.00";
        case VERSION_107: return "1.07";
        case VERSION_108: return "1.08";
        case VERSION_109: return "1.09";
        case VERSION_110: return "1.10-1.14";
        default:          return Integer.toString(versionCode);
      }
    }

    public String getVersionString() {
      return getVersionString(version);
    }

    public String getFlagsString() {
      StringBuilder sb = new StringBuilder();
      if ((flags & FLAG_BIT0)      == FLAG_BIT0)      sb.append("FLAG_BIT0|");
      if ((flags & FLAG_BIT1)      == FLAG_BIT1)      sb.append("FLAG_BIT1|");
      if ((flags & FLAG_HARDCORE)  == FLAG_HARDCORE)  sb.append("FLAG_HARDCORE|");
      if ((flags & FLAG_DIED)      == FLAG_DIED)      sb.append("FLAG_DIED|");
      if ((flags & FLAG_BIT4)      == FLAG_BIT4)      sb.append("FLAG_BIT4|");
      if ((flags & FLAG_EXPANSION) == FLAG_EXPANSION) sb.append("FLAG_EXPANSION|");
      if ((flags & FLAG_BIT6)      == FLAG_BIT6)      sb.append("FLAG_BIT6|");
      if ((flags & FLAG_BIT7)      == FLAG_BIT7)      sb.append("FLAG_BIT7|");
      if (sb.length() > 0) sb.setLength(sb.length() - 1);
      return sb.toString();
    }

    public boolean isExpansion() {
      return (flags & FLAG_EXPANSION) == FLAG_EXPANSION;
    }

    public boolean isHardcore() {
      return (flags & FLAG_HARDCORE) == FLAG_HARDCORE;
    }

    public boolean isMale() {
      switch (charClass) {
        case 2: case 3: case 4: case 5:
          return true;
        default:
          return false;
      }
    }

    public String getProgressionString() {
      int prog = (flags >>> 8) & 0xFF;
      if (isExpansion()) {
        if (prog >= 15) return isHardcore() ? "Guardian"  : isMale() ? "Patriarch" : "Matriarch";
        if (prog >= 10) return isHardcore() ? "Conqueror" : "Champion";
        if (prog >=  5) return isHardcore() ? "Destroyer" : "Slayer";
      } else {
        if (prog >= 12) return isHardcore() ? isMale() ? "King"  : "Queen"    : isMale() ? "Baron" : "Baroness";
        if (prog >=  8) return isHardcore() ? isMale() ? "Duke"  : "Duchess"  : isMale() ? "Lord"  : "Lady";
        if (prog >=  4) return isHardcore() ? isMale() ? "Count" : "Countess" : isMale() ? "Sir"   : "Dame";
      }

      return "";
    }

    public String getClassName() {
      switch (charClass) {
        case 0:  return "Amazon";
        case 1:  return "Sorceress";
        case 2:  return "Necromancer";
        case 3:  return "Paladin";
        case 4:  return "Barbarian";
        case 5:  return "Druid";
        case 6:  return "Assassin";
        default: return String.format("0x%02X", charClass);
      }
    }

    public String getTownsString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (byte town : towns) {
        sb.append('A').append((town & DIFF_ACT_MASK) + 1);
        if ((town & DIFF_FLAG_ACTIVE) == DIFF_FLAG_ACTIVE) sb.append('*');
        sb.append(", ");
      }

      sb.setLength(sb.length() - 2);
      sb.append("]");
      return sb.toString();
    }
  }

  public static class MercData {
    static final int SIZE = 16;

    static final int FLAG_DEAD = 0x100000;

    public int   flags;
    public int   seed;
    public short name;
    public short type;
    public int   xp;
    public MercItemData items;

    static MercData obtain(ByteBuffer buffer) {
      return new MercData().read(buffer);
    }

    MercData read(ByteBuffer buffer) {
      ByteBuffer slice = BufferUtils.slice(buffer, SIZE);
      flags = slice.getInt();
      seed  = slice.getInt();
      name  = slice.getShort();
      type  = slice.getShort();
      xp    = slice.getInt();
      assert !slice.hasRemaining();
      buffer.position(buffer.position() + SIZE);
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("flags", String.format("0x%02X", flags))
          .append("seed", String.format("0x%08X", seed))
          .append("name", String.format("0x%02X", name))
          .append("type", String.format("0x%02X", type))
          .append("experience", String.format("0x%08X", xp))
          .build();
    }

    public boolean isDead() {
      return (flags & FLAG_DEAD) == FLAG_DEAD;
    }

    public static class MercItemData {
      static final byte[] SECTION_HEADER = {0x6A, 0x66};

      public byte     header[];
      public ItemData items;

      static MercItemData obtain(MercData merc, ByteBuffer buffer) {
        return new MercItemData().read(merc, buffer);
      }

      MercItemData read(MercData merc, ByteBuffer buffer) {
        header = BufferUtils.readBytes(buffer, SECTION_HEADER.length);
        if (merc.seed == 0) return this;
        items = ItemData.obtain(buffer, GolemData.SECTION_HEADER, false);
        return this;
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("header", DebugUtils.toByteArray(header))
            .append("itemData", items)
            .build();
      }
    }
  }

  public static class QuestData {
    static final byte[] SECTION_HEADER = {0x57, 0x6F, 0x6F, 0x21};

    static final int NUM_QUESTFLAGS = 96;
    static final int SIZE = SECTION_HEADER.length + 6 + (NUM_QUESTFLAGS * NUM_DIFFS);

    byte  header[];
    int   version;
    short size;
    byte  data[][];

    static QuestData obtain(ByteBuffer buffer) {
      return new QuestData().read(buffer);
    }

    QuestData read(ByteBuffer buffer) {
      ByteBuffer slice = BufferUtils.slice(buffer, SIZE);
      header  = BufferUtils.readBytes(slice, SECTION_HEADER.length);
      version = slice.getInt();
      size    = slice.getShort();
      assert size == SIZE;
      data    = new byte[NUM_DIFFS][];
      for (int i = 0; i < NUM_DIFFS; i++) data[i] = BufferUtils.readBytes(slice, NUM_QUESTFLAGS);
      assert !slice.hasRemaining();
      buffer.position(buffer.position() + SIZE);
      return this;
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this)
          .append("header", DebugUtils.toByteArray(header))
          .append("version", version)
          .append("size", size);
      for (int i = 0; i < NUM_DIFFS; i++) {
        builder.append("data[" + i + "]", data[i]);
      }
      return builder.build();
    }
  }

  public static class WaypointData {
    static final byte[] SECTION_HEADER = {'W', 'S'};

    static final int SIZE = SECTION_HEADER.length + 6 + (WaypointData2.SIZE * NUM_DIFFS);

    byte  header[];
    int   version;
    short size;
    WaypointData2 diff[];

    static WaypointData obtain(ByteBuffer buffer) {
      return new WaypointData().read(buffer);
    }

    WaypointData read(ByteBuffer buffer) {
      ByteBuffer slice = BufferUtils.slice(buffer, SIZE);
      header  = BufferUtils.readBytes(slice, SECTION_HEADER.length);
      version = slice.getInt();
      size    = slice.getShort();
      assert size == SIZE;
      diff    = new WaypointData2[NUM_DIFFS];
      for (int i = 0; i < NUM_DIFFS; i++) diff[i] = WaypointData2.obtain(slice);
      assert !slice.hasRemaining();
      buffer.position(buffer.position() + SIZE);
      return this;
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this)
          .append("header", DebugUtils.toByteArray(header))
          .append("version", version)
          .append("size", size);
      for (int i = 0; i < NUM_DIFFS; i++) {
        builder.append("diff[" + i + "]", diff[i]);
      }
      return builder.build();
    }

    static class WaypointData2 {
      static final byte[] SECTION_HEADER = {0x02, 0x01};

      static final int NUM_WAYPOINTFLAGS = 22;
      static final int SIZE = SECTION_HEADER.length + NUM_WAYPOINTFLAGS;

      byte header[];
      byte data[];

      static WaypointData2 obtain(ByteBuffer buffer) {
        return new WaypointData2().read(buffer);
      }

      WaypointData2 read(ByteBuffer buffer) {
        ByteBuffer slice = BufferUtils.slice(buffer, SIZE);
        header = BufferUtils.readBytes(slice, SECTION_HEADER.length);
        data   = BufferUtils.readBytes(slice, NUM_WAYPOINTFLAGS);
        assert !slice.hasRemaining();
        buffer.position(buffer.position() + SIZE);
        return this;
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("header", DebugUtils.toByteArray(header))
            .append("data", DebugUtils.toByteArray(data))
            .build();
      }
    }
  }

  public static class NPCData {
    static final byte[] SECTION_HEADER = {0x01, 0x77};

    static final int GREETING_INTRO  = 0;
    static final int GREETING_RETURN = 1;
    static final int NUM_GREETINGS   = 2;
    static final int NUM_INTROS      = 8;
    static final int SIZE = SECTION_HEADER.length + 2 + (NUM_GREETINGS * NUM_DIFFS * NUM_INTROS);

    byte  header[];
    short size;
    byte  data[][][];

    static NPCData obtain(ByteBuffer buffer) {
      return new NPCData().read(buffer);
    }

    NPCData read(ByteBuffer buffer) {
      ByteBuffer slice = BufferUtils.slice(buffer, SIZE);
      header = BufferUtils.readBytes(slice, SECTION_HEADER.length);
      size   = slice.getShort();
      assert size == SIZE;
      data = new byte[NUM_GREETINGS][NUM_DIFFS][];
      for (int i = 0; i < NUM_GREETINGS; i++) {
        for (int j = 0; j < NUM_DIFFS; j++) {
          data[i][j] = BufferUtils.readBytes(slice, NUM_INTROS);
        }
      }

      assert !slice.hasRemaining();
      buffer.position(buffer.position() + SIZE);
      return this;
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this)
          .append("header", DebugUtils.toByteArray(header))
          .append("size", size);
      for (int i = 0; i < NUM_GREETINGS; i++) {
        for (int j = 0; j < NUM_DIFFS; j++) {
          builder.append("data[" + i + "][" + j + "][GREETING_INTRO]", DebugUtils.toByteArray(data[i][j]));
          builder.append("data[" + i + "][" + j + "][GREETING_RETURN]", DebugUtils.toByteArray(data[i][j]));
        }
      }
      return builder.build();
    }
  }

  public static class StatData {
    static final byte[] SECTION_HEADER = {0x67, 0x66};

    public byte  header[];
    public int   strength;
    public int   energy;
    public int   dexterity;
    public int   vitality;
    public int   statpts;
    public int   newskills;
    public int   hitpoints;
    public int   maxhp;
    public int   mana;
    public int   maxmana;
    public int   stamina;
    public int   maxstamina;
    public int   level;
    public long  experience;
    public int   gold;
    public int   goldbank;

    static StatData obtain(ByteBuffer buffer) {
      return new StatData().read(buffer);
    }

    StatData read(ByteBuffer slice) {
      header = BufferUtils.readBytes(slice, SECTION_HEADER.length);

      byte[] bytes = BufferUtils.readRemaining(slice);
      BitStream bitStream = new BitStream(bytes);
      for (int id; bitStream.sizeInBits() - bitStream.bitPositionInBuffer() >= 9;) {
        switch (id = bitStream.readUnsigned31OrLess(9)) {
          case 0x0: strength    = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x1: energy      = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x2: dexterity   = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x3: vitality    = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x4: statpts     = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x5: newskills   = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x6: hitpoints   = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x7: maxhp       = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x8: mana        = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x9: maxmana     = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0xA: stamina     = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0xB: maxstamina  = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0xC: level       = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0xD: experience  = bitStream.readUnsigned(numBits(id)); break;
          case 0xE: gold        = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0xF: goldbank    = bitStream.readUnsigned31OrLess(numBits(id)); break;
          default:  continue;
        }
      }

      assert !slice.hasRemaining();
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("header", DebugUtils.toByteArray(header))
          .append("strength", strength)
          .append("energy", energy)
          .append("dexterity", dexterity)
          .append("vitality", vitality)
          .append("statpts", statpts)
          .append("newskills", newskills)
          .append("hitpoints", hitpoints)
          .append("maxhp", maxhp)
          .append("mana", mana)
          .append("maxmana", maxmana)
          .append("stamina", stamina)
          .append("maxstamina", maxstamina)
          .append("level", level)
          .append("experience", experience)
          .append("gold", gold)
          .append("goldbank", goldbank)
          .build();
    }

    static int numBits(int stat) {
      return Riiablo.files.ItemStatCost.get(stat).CSvBits;
      /*switch (stat) {
        case 0: case 1: case 2: case 3: case 4:
          return 10;
        case 5:
          return 8;
        case 6: case 7: case 8: case 9: case 10: case 11:
          return 21;
        case 12:
          return 7;
        case 13:
          return 32;
        case 14: case 15:
          return 25;
        default:
          return 0;
      }*/
    }

    static float toFloat(int value) {
      return ((value >>> 8) + ((value & 0xFF) / 256f));
    }
  }

  public static class SkillData {
    static final byte[] SECTION_HEADER = {0x69, 0x66};

    public static final int NUM_TREES  = 3;
    public static final int NUM_SKILLS = 10;
    static final int SIZE = SECTION_HEADER.length + (NUM_TREES * NUM_SKILLS);

    public byte header[];
    public byte data[];

    static SkillData obtain(ByteBuffer buffer) {
      return new SkillData().read(buffer);
    }

    SkillData read(ByteBuffer buffer) {
      ByteBuffer slice = BufferUtils.slice(buffer, SIZE);
      header = BufferUtils.readBytes(slice, SECTION_HEADER.length);
      data   = BufferUtils.readBytes(slice, NUM_TREES * NUM_SKILLS);
      assert !slice.hasRemaining();
      buffer.position(buffer.position() + SIZE);
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("header", DebugUtils.toByteArray(header))
          .append("data", Arrays.toString(data))
          .build();
    }
  }

  public static class ItemData {
    static final byte[] SECTION_HEADER = {0x4A, 0x4D};
    static final byte[] SECTION_FOOTER = ArrayUtils.addAll(SECTION_HEADER, new byte[] {0x00, 0x00});

    public byte                   header[];
    public short                  size;
    public Array<Item>            items;

    static ItemData obtain(ByteBuffer buffer, byte[] SECTION_FOOTER, boolean consumeFooter) {
      return new ItemData().read(buffer, SECTION_FOOTER, consumeFooter);
    }

    ItemData read(ByteBuffer buffer, byte[] SECTION_FOOTER, boolean consumeFooter) {
      header = BufferUtils.readBytes(buffer, SECTION_HEADER.length);
      size   = buffer.getShort();

      items = new Array<>(size);
      for (int i = 0; i < size; i++) {
        ByteBuffer slice = BufferUtils.slice(buffer, SECTION_HEADER, true, SECTION_FOOTER, false);
        if (slice.remaining() <= 0) break;
        //else System.out.println(i + " = " + slice.remaining());
        byte[] bytes = BufferUtils.readRemaining(slice);
        BitStream bitStream = new BitStream(bytes);
        bitStream.skip(SECTION_HEADER.length * Byte.SIZE);
        Item item = Item.loadFromStream(bitStream);
        items.add(item);

        for (int j = 0; j < item.socketsFilled; j++) {
          slice = BufferUtils.slice(buffer, SECTION_HEADER, true, SECTION_FOOTER, false);
          if (slice.remaining() <= 0) break;
          //else System.out.println(i + " = " + slice.remaining());
          bytes = BufferUtils.readRemaining(slice);
          bitStream = new BitStream(bytes);
          bitStream.skip(SECTION_HEADER.length * Byte.SIZE);
          Item socket = Item.loadFromStream(bitStream);
          item.sockets.add(socket);
          assert socket.location == Location.SOCKET;
        }
      }
      boolean lookahead = BufferUtils.lookahead(buffer, SECTION_FOOTER);
      assert lookahead;
      if (!consumeFooter) buffer.reset();
      return this;
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this)
          .append("header", DebugUtils.toByteArray(header))
          .append("size", size)
          .append("actualSize", items.size);
      for (int i = 0; i < items.size; i++) {
        builder.append("items[" + i + "]", items.get(i).getName());
      }
      return builder.build();
    }
  }

  public static class GolemData {
    static final byte[] SECTION_HEADER = {0x6B, 0x66};

    public byte header[];
    public byte exists;
    public Item item;

    static GolemData obtain(ByteBuffer buffer) {
      return new GolemData().read(buffer);
    }

    GolemData read(ByteBuffer buffer) {
      header = BufferUtils.readBytes(buffer, SECTION_HEADER.length);
      exists = buffer.get();
      if (exists == 0) return this;
      ByteBuffer slice = BufferUtils.slice(buffer, ItemData.SECTION_HEADER, true);
      if (slice.remaining() <= 0) return this;
      byte[] bytes = BufferUtils.readRemaining(buffer);
      BitStream bitStream = new BitStream(bytes);
      bitStream.skip(ItemData.SECTION_HEADER.length * Byte.SIZE);
      item = Item.loadFromStream(bitStream);
      for (int j = 0; j < item.socketsFilled; j++) {
        slice = BufferUtils.slice(buffer, ItemData.SECTION_HEADER, true);
        if (slice.remaining() <= 0) break;
        //else System.out.println(i + " = " + slice.remaining());
        bytes = BufferUtils.readRemaining(slice);
        bitStream = new BitStream(bytes);
        bitStream.skip(ItemData.SECTION_HEADER.length * Byte.SIZE);
        Item socket = Item.loadFromStream(bitStream);
        item.sockets.add(socket);
        assert socket.location == Location.SOCKET;
      }
      return this;
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this)
          .append("header", DebugUtils.toByteArray(header))
          .append("exists", String.format("0x%02X", exists & 0xFF))
          .append("item", item)
          .build();
    }
  }
}
