package com.riiablo.attributes;

import com.riiablo.item.Item;

public class GemAttributes extends Attributes {
  private static final int NUM_GEMPROPS = Item.NUM_GEMPROPS; // TODO: move this somewhere else

  GemAttributes() {
    super(StatList.obtain(NUM_GEMPROPS));
  }

  @Override
  public void resetToBase() {
    throw new UnsupportedOperationException();
  }
}
