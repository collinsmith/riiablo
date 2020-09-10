package com.riiablo.attrs;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class StatListFlags {
  private static final Logger log = LogManager.getLogger(StatListFlags.class);

  static final int ITEM_MAGIC_LIST = 0;
  static final int ITEM_SET_LIST   = 1;
  static final int ITEM_RUNE_LIST  = 6;
  static final int NUM_ITEM_LISTS  = 7;

  static final int FLAG_NONE  = 0;
  static final int FLAG_MAGIC = 1 << ITEM_MAGIC_LIST;
  static final int FLAG_SET_2 = 1 << (ITEM_SET_LIST + 0);
  static final int FLAG_SET_3 = 1 << (ITEM_SET_LIST + 1);
  static final int FLAG_SET_4 = 1 << (ITEM_SET_LIST + 2);
  static final int FLAG_SET_5 = 1 << (ITEM_SET_LIST + 3);
  static final int FLAG_SET_6 = 1 << (ITEM_SET_LIST + 4);
  static final int FLAG_RUNE  = 1 << ITEM_RUNE_LIST;

  static final int ITEM_SET_MASK = FLAG_SET_2 | FLAG_SET_3 | FLAG_SET_4 | FLAG_SET_5 | FLAG_SET_6;

  /**
   * Unsets all set item flags and sets the flag for the currently applied set
   * item. Does not modify any other flags.
   */
  public static int getAggItemFlags(int flags, int numSetItemsEquipped) {
    flags &= ~ITEM_SET_MASK;
    flags |= getSetItemEquippedFlag(numSetItemsEquipped);
    return flags;
  }

  static String itemToString(int i) {
    switch (i) {
      case ITEM_MAGIC_LIST:
        return "ITEM_MAGIC_LIST";
      case ITEM_SET_LIST:
      case ITEM_SET_LIST + 1:
      case ITEM_SET_LIST + 2:
      case ITEM_SET_LIST + 3:
      case ITEM_SET_LIST + 4:
        return "ITEM_SET_LIST (" + (i + 1) + " items)";
      case ITEM_RUNE_LIST:
        return "ITEM_RUNE_LIST";
      default:
        assert false : "i(" + i + ") is not a valid item prop list id!";
        return String.valueOf(i);
    }
  }

  static int countSetItemFlags(int flags) {
    return Integer.bitCount(flags & ITEM_SET_MASK);
  }

  public static int getSetItemEquippedFlag(int numItems) {
    if (numItems < 0 || numItems > 6) {
      log.warn("numItems({}) not within [{}..{}]", numItems, 0, 6);
      return FLAG_NONE;
    } else if (numItems < 2) {
      return FLAG_NONE;
    }

    return 1 << (ITEM_SET_LIST + (numItems - 2));
  }

  public static int getSetItemFlags(int numItems) {
    if (numItems < 0 || numItems > 6) {
      log.warn("numItems({}) not within [{}..{}]", numItems, 0, 6);
      return FLAG_NONE;
    } else if (numItems < 2) {
      return FLAG_NONE;
    }

    return ((1 << (numItems - 1)) - 1) << ITEM_SET_LIST;
  }

  static final int GEM_WEAPON_LIST = 0;
  static final int GEM_ARMOR_LIST  = 1;
  static final int GEM_SHIELD_LIST = 2;
  static final int NUM_GEM_LISTS   = 3;

  static final int FLAG_WEAPON = 1 << GEM_WEAPON_LIST;
  static final int FLAG_ARMOR  = 1 << GEM_ARMOR_LIST;
  static final int FLAG_SHIELD = 1 << GEM_SHIELD_LIST;

  static String gemToString(int i) {
    switch (i) {
      case GEM_WEAPON_LIST:
        return "GEM_WEAPON_LIST";
      case GEM_ARMOR_LIST:
        return "GEM_ARMOR_LIST";
      case GEM_SHIELD_LIST:
        return "GEM_SHIELD_LIST";
      default:
        assert false : "i(" + i + ") is not a valid gem prop list id!";
        return String.valueOf(i);
    }
  }

  static int countGemFlags(int flags) {
    return Integer.bitCount(flags);
  }

  private StatListFlags() {}
}
