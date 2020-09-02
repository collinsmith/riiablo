package com.riiablo.attributes;

public class GemAttributes extends Attributes {
  private static final int NUM_GEMPROPS = 3; // TODO: move this somewhere else

  GemAttributes() {
    super(StatList.obtain(NUM_GEMPROPS));
  }

  @Override
  public void resetToBase() {
    throw new UnsupportedOperationException();
  }
}
