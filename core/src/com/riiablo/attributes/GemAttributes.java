package com.riiablo.attributes;

public class GemAttributes extends Attributes {
  GemAttributes() {
    super(StatList.obtain(StatListFlags.NUM_GEMPROPS));
  }

  @Override
  public void resetToBase() {
    throw new UnsupportedOperationException();
  }
}
