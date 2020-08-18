package com.riiablo.save.d2s;

import com.badlogic.gdx.utils.Array;

import com.riiablo.Riiablo;
import com.riiablo.item.Item;

public class D2S {
  public static final String EXT = "d2s";

  static final byte[] SIGNATURE = {0x55, (byte) 0xAA, 0x55, (byte) 0xAA};

  static final int VERSION_100 = 71;
  static final int VERSION_107 = 87;
  static final int VERSION_108 = 89;
  static final int VERSION_109 = 92;
  static final int VERSION_110 = 96;

  static final int FLAG_BIT0      = 1 << 0;
  static final int FLAG_BIT1      = 1 << 1;
  static final int FLAG_HARDCORE  = 1 << 2;
  static final int FLAG_DIED      = 1 << 3;
  static final int FLAG_BIT4      = 1 << 4;
  static final int FLAG_EXPANSION = 1 << 5;
  static final int FLAG_BIT6      = 1 << 6;
  static final int FLAG_BIT7      = 1 << 7;

  static final int PRIMARY = 0;
  static final int SECONDARY = 1;
  static final int NUM_ALTS = 2;
  static final int NUM_ACTIONS = NUM_ALTS;
  static final int NUM_BUTTONS = 2;
  static final int NUM_HOTKEYS = 16;
  static final int NUM_DIFFS = Riiablo.NUM_DIFFS;

  int version;
  int size;
  int checksum;
  int alternate;
  String name;
  int flags;
  byte charClass;
  byte level;
  int timestamp;
  int[] hotkeys;
  int[][] actions;
  byte[] composites;
  byte[] colors;
  byte[] towns;
  int mapSeed;
  MercData merc;
  QuestData quests;
  WaypointData waypoints;
  NPCData npcs;
  StatData stats;
  SkillData skills;
  ItemData items;
  ItemData corpse;
  GolemData golem;

  public static String getVersionString(int versionCode) {
    switch (versionCode) {
      case VERSION_100: return "1.00";
      case VERSION_107: return "1.07";
      case VERSION_108: return "1.08";
      case VERSION_109: return "1.09";
      case VERSION_110: return "1.10-1.14";
      default: return Integer.toString(versionCode);
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

  public boolean hasFlag(int flag) {
    return (flags & flag) == flag;
  }

  public boolean isHardcore() {
    return hasFlag(FLAG_HARDCORE);
  }

  public boolean isExpansion() {
    return isExpansion();
  }

  public boolean isMale() {
    switch (charClass) {
      case Riiablo.NECROMANCER:
      case Riiablo.PALADIN:
      case Riiablo.BARBARIAN:
      case Riiablo.DRUID:
        return true;
      default:
        return false;
    }
  }

  public String getProgressionString() {
    int prog = (flags >>> 8) & 0xFF;
    final boolean hc = isHardcore();
    final boolean male = isMale();
    if (isExpansion()) {
      if (prog >= 15) return hc ? "Guardian" : male ? "Patriarch" : "Matriarch";
      if (prog >= 10) return hc ? "Conqueror" : "Champion";
      if (prog >= 5) return hc ? "Destroyer" : "Slayer";
    } else {
      if (prog >= 12) return hc ? male ? "King" : "Queen" : male ? "Baron" : "Baroness";
      if (prog >= 8) return hc ? male ? "Duke" : "Duchess" : male ? "Lord" : "Lady";
      if (prog >= 4) return hc ? male ? "Count" : "Countess" : male ? "Sir" : "Dame";
    }

    return "";
  }

  public String getTownsString() {
    final int DIFF_ACT_MASK = 0x7;
    final int DIFF_FLAG_ACTIVE = 1 << 7;
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

  public static class MercData {
    int flags;
    int seed;
    short name;
    short type;
    int experience;
    ItemData items;
  }

  public static class QuestData {
    static final int NUM_QUESTFLAGS = 96;

    byte[][] flags;
  }

  public static class WaypointData {
    static final int NUM_WAYPOINTFLAGS = 22;

    byte[][] flags;
  }

  public static class NPCData {
    static final int GREETING_INTRO = 0;
    static final int GREETING_RETURN = 1;
    static final int NUM_GREETINGS = 2;
    static final int NUM_INTROS = 8;


    public static String getGreetingString(int type) {
      switch (type) {
        case GREETING_INTRO: return "INTRO";
        case GREETING_RETURN: return "RETURN";
        default:
          throw new IllegalArgumentException(
              String.format("type(%d) is not GREETING_INTRO(%d) or GREETING_RETURN(%d)",
                  type, GREETING_INTRO, GREETING_RETURN));
      }
    }

    byte[][][] flags;
  }

  public static class StatData {
    public int strength;
    public int energy;
    public int dexterity;
    public int vitality;
    public int statpts;
    public int newskills;
    public int hitpoints;
    public int maxhp;
    public int mana;
    public int maxmana;
    public int stamina;
    public int maxstamina;
    public int level;
    public long experience;
    public int gold;
    public int goldbank;
  }

  public static class SkillData {
    static final int NUM_TREES = 3;
    static final int NUM_SKILLS = 10;

    byte[] skills;
  }

  public static class ItemData {
    Array<Item> items;
  }

  public static class GolemData {
    boolean exists;
    Item item;
  }
}
