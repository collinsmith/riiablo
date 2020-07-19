package com.riiablo.item;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.codec.util.BitStream;

class RareQualityData {
  static final int NUM_AFFIXES = 3;
  int[] prefixes, suffixes;
  RareQualityData(BitStream bitStream) {
    prefixes = new int[NUM_AFFIXES];
    suffixes = new int[NUM_AFFIXES];
    for (int i = 0; i < NUM_AFFIXES; i++) {
      prefixes[i] = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(Item.MAGIC_AFFIX_SIZE) : 0;
      suffixes[i] = bitStream.readBoolean() ? bitStream.readUnsigned15OrLess(Item.MAGIC_AFFIX_SIZE) : 0;
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("prefixes", prefixes)
        .append("suffixes", suffixes)
        .build();
  }
}
