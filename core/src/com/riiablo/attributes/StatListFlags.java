package com.riiablo.attributes;

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
  static final int FLAG_SET_2 = 1 << ITEM_SET_LIST + 0;
  static final int FLAG_SET_3 = 1 << ITEM_SET_LIST + 1;
  static final int FLAG_SET_4 = 1 << ITEM_SET_LIST + 2;
  static final int FLAG_SET_5 = 1 << ITEM_SET_LIST + 3;
  static final int FLAG_SET_6 = 1 << ITEM_SET_LIST + 4;
  static final int FLAG_RUNE  = 1 << ITEM_RUNE_LIST;

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

  public int getItemSetFlags(int numItems) {
    int flags = FLAG_NONE;
    switch (numItems) {
      case 6: flags |= FLAG_SET_6; // fall-through
      case 5: flags |= FLAG_SET_5; // fall-through
      case 4: flags |= FLAG_SET_4; // fall-through
      case 3: flags |= FLAG_SET_3; // fall-through
      case 2: flags |= FLAG_SET_2;
        return flags;
      default:
        log.warn("numItems({}) not within [2..6]", numItems);
        return flags;
    }
  }

  static final int GEM_WEAPON_LIST = 0;
  static final int GEM_ARMOR_LIST  = 1;
  static final int GEM_SHIELD_LIST = 2;
  static final int NUM_GEM_LISTS   = 3;

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

  private StatListFlags() {}
}
