package com.riiablo.item.item4;

class RunewordData {
  static final int RUNEWORD_ID_SHIFT    = 0;
  static final int RUNEWORD_ID_MASK     = 0xFFF << RUNEWORD_ID_SHIFT;
  static final int RUNEWORD_EXTRA_SHIFT = 12;
  static final int RUNEWORD_EXTRA_MASK  = 0xF << RUNEWORD_EXTRA_SHIFT;

  static int id(int pack) {
    return (pack & RUNEWORD_ID_MASK) >>> RUNEWORD_ID_SHIFT;
  }

  static int extra(int pack) {
    return (pack & RUNEWORD_EXTRA_MASK) >>> RUNEWORD_EXTRA_SHIFT;
  }
}
