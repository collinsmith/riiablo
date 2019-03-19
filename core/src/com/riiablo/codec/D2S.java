package com.riiablo.codec;

import com.google.common.primitives.Ints;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.riiablo.Riiablo;
import com.riiablo.codec.util.BitStream;
import com.riiablo.entity.Player;
import com.riiablo.item.BodyLoc;
import com.riiablo.item.Item;
import com.riiablo.item.Location;
import com.riiablo.item.StoreLoc;
import com.riiablo.util.BufferUtils;
import com.riiablo.util.DebugUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;

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

  public static final String EXT = "d2s";

  static final int HEADER_SIZE = 0x14F;

  static final int VERSION_100 = 71;
  static final int VERSION_107 = 87;
  static final int VERSION_108 = 89;
  static final int VERSION_109 = 92;
  static final int VERSION_110 = 96;

  static final int MAGIC_NUMBER = 0xAA55AA55;

  static final int FLAG_BIT0      = 1 << 0;
  static final int FLAG_BIT1      = 1 << 1;
  static final int FLAG_HARDCORE  = 1 << 2;
  static final int FLAG_DIED      = 1 << 3;
  static final int FLAG_BIT4      = 1 << 4;
  static final int FLAG_EXPANSION = 1 << 5;
  static final int FLAG_BIT6      = 1 << 6;
  static final int FLAG_BIT7      = 1 << 7;

  static final int SKILL_UNASSIGNED = 0xFFFF;
  static final int NUM_SKILLBAR_SKILLS = 16;

  static final int ACTION_PRIMARY   = 0;
  static final int ACTION_SECONDARY = 1;
  static final int NUM_ACTIONS      = 2;

  static final int BUTTON_LEFT      = 0;
  static final int BUTTON_RIGHT     = 1;
  static final int NUM_BUTTONS      = 2;

  static final int NUM_DIFFICULTIES = 3;  // TODO: Point at Diablo.MAX_DIFFICULTIES or something
  static final int DIFF_ACT_MASK    = 0x7;
  static final int DIFF_FLAG_ACTIVE = 1 << 7;

  public static final int COMPOSITE_UNASSIGNED = 0xFF;
  public static final int COLOR_UNASSIGNED     = 0xFF;

  public int      magicNumber;
  public int      version;
  public int      size;
  public int      checksum;
  public int      weaponSlot;
  public String   name;
  public int      flags;
  public byte     charClass;
  public byte     unk1[];
  public byte     level;
  public byte     unk2[];
  public int      timestamp;
  public byte     unk3[];
  public int      skillBar[];
  public int      actions[][];
  public byte     composites[];
  public byte     colors[];
  public byte     towns[];
  public int      mapSeed;
  public MercData merc;
  public byte     realmData[];

  public QuestData    quests;
  public WaypointData waypoints;
  public NPCData      npcs;
  public StatData     stats;
  public SkillData    skills;
  public ItemData     items;

  private D2S() {}

  public static D2S loadFromFile(FileHandle file) {
    return loadFromArray(file.readBytes());
  }

  public static D2S loadFromStream(InputStream in) {
    try {
      return loadFromArray(StreamUtils.copyStreamToByteArray(in));
    } catch (Throwable t) {
      throw new GdxRuntimeException("Couldn't read D2S from stream.", t);
    } finally {
      StreamUtils.closeQuietly(in);
    }
  }

  public static D2S loadFromArray(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    D2S d2s = new D2S().read(buffer);
    if (d2s.magicNumber != MAGIC_NUMBER) throw new GdxRuntimeException("Magic number doesn't match " + String.format("0x%08X", MAGIC_NUMBER) + ": " + String.format("0x%08X", d2s.magicNumber));
    if (d2s.version != VERSION_110) throw new GdxRuntimeException("Unsupported D2S version: " + d2s.version + " -- Only supports " + getVersionString(VERSION_110));
    if (d2s.size != bytes.length) Gdx.app.error(TAG, "Save file size doesn't match encoded size for character " + d2s.name + ". Should be: " + d2s.size);
    if (DEBUG_HEADER)    Gdx.app.debug(TAG, d2s.toString());
    if (DEBUG_QUESTS)    Gdx.app.debug(TAG, d2s.quests.toString());
    if (DEBUG_WAYPOINTS) Gdx.app.debug(TAG, d2s.waypoints.toString());
    if (DEBUG_NPCS)      Gdx.app.debug(TAG, d2s.npcs.toString());
    if (DEBUG_STATS)     Gdx.app.debug(TAG, d2s.stats.toString());
    if (DEBUG_SKILLS)    Gdx.app.debug(TAG, d2s.skills.toString());
    if (DEBUG_ITEMS) {
      Gdx.app.debug(TAG, d2s.items.toString());
      for (Item item : d2s.items.items) {
        Gdx.app.debug(TAG, item.toString());
      }
    }
    return d2s;
  }

  private D2S read(ByteBuffer buffer) {
    magicNumber   = buffer.getInt();
    version       = buffer.getInt();
    size          = buffer.getInt();
    checksum      = buffer.getInt();
    weaponSlot    = buffer.getInt();
    name          = BufferUtils.readString2(buffer, Player.MAX_NAME_LENGTH + 1);
    flags         = buffer.getInt();
    charClass     = buffer.get();
    unk1          = BufferUtils.readBytes(buffer, 2);
    level         = buffer.get();
    unk2          = BufferUtils.readBytes(buffer, Ints.BYTES);
    timestamp     = buffer.getInt();
    unk3          = BufferUtils.readBytes(buffer, Ints.BYTES);
    skillBar      = BufferUtils.readInts(buffer, NUM_SKILLBAR_SKILLS);
    actions       = new int[NUM_ACTIONS][NUM_BUTTONS];
    for (int i = 0; i < NUM_ACTIONS; i++) actions[i] = BufferUtils.readInts(buffer, NUM_BUTTONS);
    composites    = BufferUtils.readBytes(buffer, com.riiablo.codec.COF.Component.NUM_COMPONENTS);
    colors        = BufferUtils.readBytes(buffer, COF.Component.NUM_COMPONENTS);
    towns         = BufferUtils.readBytes(buffer, NUM_DIFFICULTIES);
    mapSeed       = buffer.getInt();
    merc          = MercData.obtain(buffer);
    realmData     = BufferUtils.readBytes(buffer, 144);
    assert buffer.position() == HEADER_SIZE;

    quests    = QuestData.obtain(buffer);
    waypoints = WaypointData.obtain(buffer);
    npcs      = NPCData.obtain(buffer);
    stats     = StatData.obtain(BufferUtils.slice(buffer, SkillData.SECTION_HEADER));
    skills    = SkillData.obtain(buffer);
    items     = ItemData.obtain(buffer);

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
        .append("skillBar", Arrays.toString(skillBar))
        .append("actions[ACTION_PRIMARY]", Arrays.toString(actions[ACTION_PRIMARY]))
        .append("actions[ACTION_SECONDARY]", Arrays.toString(actions[ACTION_SECONDARY]))
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

  static class MercData {
    static final int SIZE = 16;

    static final int FLAG_DEAD = 0x100000;

    int   flags;
    int   seed;
    short name;
    short type;
    int   xp;

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
          .append("xp", String.format("0x%08X", xp))
          .build();
    }

    public boolean isDead() {
      return (flags & FLAG_DEAD) == FLAG_DEAD;
    }
  }

  static class QuestData {
    static final byte[] SECTION_HEADER = {0x57, 0x6F, 0x6F, 0x21};

    static final int NUM_QUESTFLAGS = 96;
    static final int SIZE = SECTION_HEADER.length + 6 + (NUM_QUESTFLAGS * NUM_DIFFICULTIES);

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
      data    = new byte[NUM_DIFFICULTIES][];
      for (int i = 0; i < NUM_DIFFICULTIES; i++) data[i] = BufferUtils.readBytes(slice, NUM_QUESTFLAGS);
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
      for (int i = 0; i < NUM_DIFFICULTIES; i++) {
        builder.append("data[" + i + "]", data[i]);
      }
      return builder.build();
    }
  }

  static class WaypointData {
    static final byte[] SECTION_HEADER = {'W', 'S'};

    static final int SIZE = SECTION_HEADER.length + 6 + (WaypointData2.SIZE * NUM_DIFFICULTIES);

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
      diff    = new WaypointData2[NUM_DIFFICULTIES];
      for (int i = 0; i < NUM_DIFFICULTIES; i++) diff[i] = WaypointData2.obtain(slice);
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
      for (int i = 0; i < NUM_DIFFICULTIES; i++) {
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

  static class NPCData {
    static final byte[] SECTION_HEADER = {0x01, 0x77};

    static final int GREETING_INTRO  = 0;
    static final int GREETING_RETURN = 1;
    static final int NUM_GREETINGS   = 2;
    static final int NUM_INTROS      = 8;
    static final int SIZE = SECTION_HEADER.length + 2 + (NUM_GREETINGS * NUM_DIFFICULTIES * NUM_INTROS);

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
      data = new byte[NUM_GREETINGS][NUM_DIFFICULTIES][];
      for (int i = 0; i < NUM_GREETINGS; i++) {
        for (int j = 0; j < NUM_DIFFICULTIES; j++) {
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
        for (int j = 0; j < NUM_DIFFICULTIES; j++) {
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
    public int   statPoints;
    public int   skillPoints;
    public float life;
    public float baseLife;
    public float mana;
    public float baseMana;
    public float stamina;
    public float baseStamina;
    public int   level;
    public long  xp;
    public int   invGold;
    public int   stashGold;

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
          case 0x4: statPoints  = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x5: skillPoints = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0x6: life        = toFloat(bitStream.readUnsigned31OrLess(numBits(id))); break;
          case 0x7: baseLife    = toFloat(bitStream.readUnsigned31OrLess(numBits(id))); break;
          case 0x8: mana        = toFloat(bitStream.readUnsigned31OrLess(numBits(id))); break;
          case 0x9: baseMana    = toFloat(bitStream.readUnsigned31OrLess(numBits(id))); break;
          case 0xA: stamina     = toFloat(bitStream.readUnsigned31OrLess(numBits(id))); break;
          case 0xB: baseStamina = toFloat(bitStream.readUnsigned31OrLess(numBits(id))); break;
          case 0xC: level       = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0xD: xp          = bitStream.readUnsigned(numBits(id)); break;
          case 0xE: invGold     = bitStream.readUnsigned31OrLess(numBits(id)); break;
          case 0xF: stashGold   = bitStream.readUnsigned31OrLess(numBits(id)); break;
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
          .append("statPoints", statPoints)
          .append("skillPoints", skillPoints)
          .append("life", life)
          .append("baseLife", baseLife)
          .append("mana", mana)
          .append("baseMana", baseMana)
          .append("stamina", stamina)
          .append("baseStamina", baseStamina)
          .append("level", level)
          .append("xp", xp)
          .append("invGold", invGold)
          .append("stashGold", stashGold)
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

    static final int NUM_TREES  = 3;
    static final int NUM_SKILLS = 10;
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
    public EnumMap<BodyLoc, Item> equipped;
    public Array<Item>            inventory;

    static ItemData obtain(ByteBuffer buffer) {
      return new ItemData().read(buffer);
    }

    ItemData read(ByteBuffer buffer) {
      header = BufferUtils.readBytes(buffer, SECTION_HEADER.length);
      size   = buffer.getShort();

      items = new Array<>(size);
      equipped = new EnumMap<>(BodyLoc.class);
      inventory = new Array<>();
      for (int i = 0; i < size; i++) {
        ByteBuffer slice = BufferUtils.slice(buffer, SECTION_HEADER, true);
        if (slice.remaining() <= 0) break;
        //else System.out.println(i + " = " + slice.remaining());
        byte[] bytes = BufferUtils.readRemaining(slice);
        BitStream bitStream = new BitStream(bytes);
        bitStream.skip(SECTION_HEADER.length * Byte.SIZE);
        Item item = Item.loadFromStream(bitStream);
        items.add(item);
        if (item.location == Location.EQUIPPED && item.bodyLoc != BodyLoc.NONE) {
          equipped.put(item.bodyLoc, item);
        } else if (item.location == Location.STORED && item.storeLoc == StoreLoc.INVENTORY) {
          inventory.add(item);
        }

        for (int j = 0; j < item.socketsFilled; j++) {
          slice = BufferUtils.slice(buffer, SECTION_HEADER, true);
          if (slice.remaining() <= 0) break;
          //else System.out.println(i + " = " + slice.remaining());
          bytes = BufferUtils.readRemaining(slice);
          bitStream = new BitStream(bytes);
          bitStream.skip(SECTION_HEADER.length * Byte.SIZE);
          Item socket = Item.loadFromStream(bitStream);
          item.socketed.add(socket);
          assert socket.location == Location.SOCKET;
        }

        //if (BufferUtils.lookahead(buffer, SECTION_FOOTER)) {
        //  break;
        //}
      }
      assert BufferUtils.lookahead(buffer, SECTION_FOOTER);
      //assert !buffer.hasRemaining();
      return this;
    }

    @Override
    public String toString() {
      ToStringBuilder builder = new ToStringBuilder(this)
          .append("header", DebugUtils.toByteArray(header))
          .append("size", size)
          .append("actualSize", items.size);
      for (int i = 0; i < items.size; i++) {
        builder.append("items[" + i + "]", items.get(i).type);
      }
      return builder.build();
    }

    /*
    public static class Item {
      static final byte[] SECTION_HEADER = ItemData.SECTION_HEADER;

      public static final int RARE_AFFIXES = 3;
      private static final int AFFIX_FOOTER = 0x1FF;

      static final int NUM_PROPS = 7;
      static final int NUM_SET_PROPS = 5;
      static final int PROP_MAGIC = 1 << 0;
      static final int PROP_SET[] = { 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5 };
      static final int PROP_RUNE  = 1 << 6;
      static String getPropListName(int prop) {
        switch (prop) {
          case 0:  return "PROP_MAGIC";
          case 1:  return "PROP_SET[0]";
          case 2:  return "PROP_SET[1]";
          case 3:  return "PROP_SET[2]";
          case 4:  return "PROP_SET[3]";
          case 5:  return "PROP_SET[4]";
          case 6:  return "PROP_RUNE";
          default: return Integer.toString(prop);
        }
      }

      public byte    header[];
      public boolean quest;
      public boolean identified;
      public boolean socketed;
      public boolean unsaved;
      public boolean ear;
      public boolean starting;
      public boolean compact;
      public boolean ethereal;
      public boolean personalized;
      public boolean runeword;
      public short   version;
      public byte    location;
      public byte    equipped;
      public byte    x;
      public byte    y;
      public byte    stored;
      public String  type;
      public byte    socketsUsed;

      public byte    earClass;
      public byte    earLevel;
      public String  earName;

      public int     id;
      public byte    ilvl;
      public byte    quality;

      public boolean usePictureId;
      public byte    pictureId;

      public boolean classSpecific;
      public short   classSpecificFlags;

      public byte    lowQuality;
      public byte    highQuality;
      public short   magicPrefix;
      public short   magicSuffix;
      public short   setId;
      public short   uniqueId;

      public RareItemData rareData;
      public RareItemData craftedData;

      public short   runeId;
      public byte    runeUnk;

      public String  personalization;

      public short   armorRating;

      public short   maxDurability;
      public short   curDurability;

      public byte    sockets;

      public byte    tomes;

      public short   quantity;

      public byte    setAffixes;

      public IntMap<Stat> affixes;

      static Item obtain(BitStream bitStream) {
        return new Item().read(bitStream);
      }

      Item read(BitStream bitStream) {
        header = bitStream.readFully(SECTION_HEADER.length);
        if (true) {
          System.out.println(com.riiablo.item.Item.loadFromStream(bitStream));
          return this;
        }

        quest = bitStream.readBoolean();
        bitStream.skip(3);
        identified = bitStream.readBoolean();
        bitStream.skip(6);
        socketed = bitStream.readBoolean();
        bitStream.skip(1);
        unsaved = bitStream.readBoolean();
        bitStream.skip(2);
        ear = bitStream.readBoolean();
        starting = bitStream.readBoolean();
        bitStream.skip(3);
        compact = bitStream.readBoolean();
        ethereal = bitStream.readBoolean();
        bitStream.skip(1);
        personalized = bitStream.readBoolean();
        bitStream.skip(1);
        runeword = bitStream.readBoolean();
        bitStream.skip(5);
        version = bitStream.readUnsigned15OrLess(8);
        bitStream.skip(2);
        location = bitStream.readUnsigned7OrLess(3);
        equipped = bitStream.readUnsigned7OrLess(4);
        x = bitStream.readUnsigned7OrLess(4);
        y = bitStream.readUnsigned7OrLess(3);
        bitStream.skip(1);
        stored = bitStream.readUnsigned7OrLess(3);

        if (ear) {
          type = "ear";
          socketsUsed = 0;
          earClass = bitStream.readUnsigned7OrLess(3);
          earLevel = bitStream.readUnsigned7OrLess(7);
          earName  = bitStream.readString(16, 7);
        } else {
          type = bitStream.readString(4).trim();
          socketsUsed = bitStream.readUnsigned7OrLess(3);
        }

        if (compact) {
          return this;
        }

        id      = (int) bitStream.readUnsigned(32);
        ilvl    =       bitStream.readUnsigned7OrLess(7);
        quality =       bitStream.readUnsigned7OrLess(4);

        usePictureId = bitStream.readBoolean();
        if (usePictureId) pictureId = bitStream.readUnsigned7OrLess(3);

        classSpecific = bitStream.readBoolean();
        if (classSpecific) classSpecificFlags = bitStream.readUnsigned15OrLess(11);

        int props = PROP_MAGIC;
        switch (quality) {
          case Quality.LOW:   lowQuality  = bitStream.readUnsigned7OrLess(3); break;
          case Quality.HIGH:  highQuality = bitStream.readUnsigned7OrLess(3); break;
          case Quality.MAGIC:
            magicPrefix = bitStream.readUnsigned15OrLess(11);
            magicSuffix = bitStream.readUnsigned15OrLess(11);
            break;
          case Quality.SET:
            setId = bitStream.readUnsigned15OrLess(12);
            break;
          case Quality.RARE:
            rareData = new RareItemData(bitStream);
            break;
          case Quality.UNIQUE:
            uniqueId = bitStream.readUnsigned15OrLess(12);
            break;
          case Quality.CRAFTED:
            craftedData = new RareItemData(bitStream);
            break;
        }

        if (runeword) {
          runeId = bitStream.readUnsigned15OrLess(12);
          runeUnk = bitStream.readUnsigned7OrLess(4);
          props |= PROP_RUNE;
        }

        if (personalized) {
          personalization = bitStream.readString(16, 7);
        }

        bitStream.skip(1);

        ItemEntry itemEntry;
        if ((itemEntry = Diablo.files.weapons.get(type)) != null) {
        } else if ((itemEntry = Diablo.files.armor.get(type)) != null) {
        } else if ((itemEntry = Diablo.files.misc.get(type)) != null) {
        }

        //System.out.println(type);
        ItemTypes.Entry itemType = Diablo.files.ItemTypes.get(itemEntry.type);
        if (itemType.is("armo")) {
          ItemStatCost.Entry armorclass = Diablo.files.ItemStatCost.get("armorclass");
          armorRating = bitStream.readUnsigned15OrLess(armorclass.Save_Bits);
          //System.out.println((armorRating - armorclass.Save_Add) + " defense");
        }

        if (itemType.is("armo") || itemType.is("weap")) {
          ItemStatCost.Entry durability = Diablo.files.ItemStatCost.get("durability");
          ItemStatCost.Entry mDurability = Diablo.files.ItemStatCost.get("maxdurability");
          maxDurability = bitStream.readUnsigned15OrLess(mDurability.Save_Bits);
          if (maxDurability > 0)
             curDurability = bitStream.readUnsigned15OrLess(durability.Save_Bits);
          //System.out.println("durability " + (curDurability - durability.Save_Add) + "/" + (maxDurability - mDurability.Save_Add));
        }

        if (socketed) {
          ItemStatCost.Entry socket = Diablo.files.ItemStatCost.get("item_numsockets");
          sockets = bitStream.readUnsigned7OrLess(socket.Save_Bits);
          //System.out.println((sockets - socket.Save_Add) + " sockets");
        }

        if (itemType.is("book")) tomes = bitStream.readUnsigned7OrLess(5);
        if (itemEntry.stackable) quantity = bitStream.readUnsigned15OrLess(9);

        if (quality == Quality.SET) {
          setAffixes = bitStream.readUnsigned7OrLess(NUM_SET_PROPS);
          props |= (setAffixes << 1);
          //System.out.println("setAffixes " + Integer.toBinaryString(setAffixes));
        }

        affixes = new IntMap<>();
        for (int i = 0, j = PROP_MAGIC; i < NUM_PROPS; i++, j <<= 1) {
          if ((props & j) == j) {
            //System.out.println(getPropListName(i));
            for (short id; (id = bitStream.readUnsigned15OrLess(9)) != AFFIX_FOOTER;) {
              read(bitStream, affixes, id);
            }
          }
        }

        //System.out.println();

        return this;
      }

      public void read(BitStream bitStream, IntMap<Stat> affixes, int id) {
        final int len = Stat.getStatCount(id);
        for (int i = 0; i < len; i++, id++) {
          ItemStatCost.Entry statEntry = Diablo.files.ItemStatCost.get(id);
          int value = bitStream.readUnsigned31OrLess(statEntry.Save_Bits);
          int param = bitStream.readUnsigned31OrLess(statEntry.Save_Param_Bits);
          Stat stat = new Stat(statEntry, value, param);
          affixes.put(id, stat);
          //System.out.println(stat);
        }
      }

      @Override
      public String toString() {
        return new ToStringBuilder(this)
            .append("header", DebugUtils.toByteArray(header))
            .append("type", type)
            .append("id", String.format("0x%08X", id))
            .append("ilvl", ilvl)
            .append("quality", Quality.toString(quality))
            .append("version", version)
            .append("quest", quest)
            .append("identified", identified)
            .append("socketed", socketed)
            .append("unsaved", unsaved)
            .append("ear", ear)
            .append("starting", starting)
            .append("compact", compact)
            .append("ethereal", ethereal)
            .append("personalized", personalized)
            .append("runeword", runeword)
            .append("location", Location.toString(location))
            .append("equipped", getEquippedString())
            .append("x", x)
            .append("y", y)
            .append("stored", Store.toString(stored))
            .append("usePictureId", usePictureId)
            .append("pictureId", pictureId)
            .append("classSpecific", classSpecific)
            .append("classSpecificFlags", String.format("0x%03X", classSpecificFlags))
            .append("lowQuality", Quality.Low.toString(lowQuality))
            .append("highQuality", highQuality)
            .append("magicPrefix", magicPrefix)
            .append("magicSuffix", magicSuffix)
            .append("setId", setId)
            .append("uniqueId", uniqueId)
            .append("rareData", rareData)
            .append("runeId", runeId)
            .append("runeUnk", runeUnk)
            .append("armorRating", armorRating)
            .append("maxDurability", maxDurability)
            .append("curDurability", curDurability)
            .append("sockets", sockets)
            .append("tomes", tomes)
            .append("quantity", quantity)
            .append("setAffixes", setAffixes)
            .build();
      }

      public String getEquippedString() {
        return Player.Slot.toString(equipped);
      }

      public static class RareItemData {
        public short rarePrefix;
        public short rareSuffix;
        public short magicPrefix[];
        public short magicSuffix[];

        public RareItemData(BitStream bitStream) {
          rarePrefix = bitStream.readUnsigned15OrLess(8);
          rareSuffix = bitStream.readUnsigned15OrLess(8);

          magicPrefix = new short[RARE_AFFIXES];
          magicSuffix = new short[RARE_AFFIXES];
          for (int i = 0; i < RARE_AFFIXES; i++) {
            if (bitStream.readBoolean()) magicPrefix[i] = bitStream.readUnsigned15OrLess(11);
            if (bitStream.readBoolean()) magicSuffix[i] = bitStream.readUnsigned15OrLess(11);
          }
        }

        @Override
        public String toString() {
          return new ToStringBuilder(this)
              .append("rarePrefix", rarePrefix)
              .append("rareSuffix", rareSuffix)
              .append("magicPrefix", Arrays.toString(magicPrefix))
              .append("magicSuffix", Arrays.toString(magicSuffix))
              .build();
        }
      }
    }
  */
  }
}
