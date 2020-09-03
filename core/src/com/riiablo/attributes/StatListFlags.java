package com.riiablo.attributes;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public class StatListFlags {
  private static final Logger log = LogManager.getLogger(StatListFlags.class);

  static final int MAGIC_LIST = 0;
  static final int SET_LIST   = 1;
  static final int RUNE_LIST  = 6;
  static final int NUM_LISTS  = 7;

  static final int MAGIC = 1 << MAGIC_LIST;
  static final int SET_2 = 1 << SET_LIST + 0;
  static final int SET_3 = 1 << SET_LIST + 1;
  static final int SET_4 = 1 << SET_LIST + 2;
  static final int SET_5 = 1 << SET_LIST + 3;
  static final int SET_6 = 1 << SET_LIST + 4;
  static final int RUNE  = 1 << RUNE_LIST;

  static final int GEMPROPS_WEAPON  = 0;
  static final int GEMPROPS_ARMOR   = 1;
  static final int GEMPROPS_SHIELD  = 2;
  static final int NUM_GEMPROPS     = 3;

  static String toString(int i) {
    switch (i) {
      case MAGIC_LIST: return "MAGIC_LIST";
      case SET_LIST:
      case SET_LIST + 1:
      case SET_LIST + 2:
      case SET_LIST + 3:
      case SET_LIST + 4:
        return "SET_LIST (" + (i + 1) + " items)";
      case RUNE_LIST:
        return "RUNE_LIST";
      default:
        assert false : "i(" + i + ") is not a valid prop list id!";
        return String.valueOf(i);
    }
  }

  public int getSetFlags(int numItems) {
    int flags = 0;
    switch (numItems) {
      default:
        log.warn("numItems({}) not within [2..6]", numItems);
      case 6: flags |= SET_6; // fall-through
      case 5: flags |= SET_5; // fall-through
      case 4: flags |= SET_4; // fall-through
      case 3: flags |= SET_3; // fall-through
      case 2: flags |= SET_2;
        return flags;
    }
  }

  private StatListFlags() {}
}
