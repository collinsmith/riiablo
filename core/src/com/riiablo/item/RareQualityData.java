package com.riiablo.item;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.riiablo.io.BitInput;

class RareQualityData {
  static final int NUM_AFFIXES = 3;
  int[] prefixes, suffixes;
  RareQualityData(BitInput bitStream) {
    prefixes = new int[NUM_AFFIXES];
    suffixes = new int[NUM_AFFIXES];
    for (int i = 0; i < NUM_AFFIXES; i++) {
      prefixes[i] = bitStream.readBoolean() ? bitStream.read15u(Item.MAGIC_AFFIX_SIZE) : 0;
      suffixes[i] = bitStream.readBoolean() ? bitStream.read15u(Item.MAGIC_AFFIX_SIZE) : 0;
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
