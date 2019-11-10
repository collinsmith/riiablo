package com.riiablo.engine;

public final class Flags {
  public static final int NONE       = 0;
  public static final int DEBUG      = 1 << 0;
  public static final int SELECTABLE = 1 << 1;
  public static final int SELECTED   = 1 << 2;
  public static final int INVISIBLE  = 1 << 3;
  public static final int RUNNING    = 1 << 4;

  public static String toString(int bits) {
    StringBuilder builder = new StringBuilder();
    if (bits == NONE) {
      builder.append("NONE");
    } else {
      if ((bits & DEBUG) == DEBUG) builder.append("DEBUG").append("|");
      if ((bits & SELECTABLE) == SELECTABLE) builder.append("SELECTABLE").append("|");
      if ((bits & SELECTED) == SELECTED) builder.append("SELECTED").append("|");
      if ((bits & INVISIBLE) == INVISIBLE) builder.append("INVISIBLE").append("|");
      if ((bits & RUNNING) == RUNNING) builder.append("RUNNING").append("|");
      if (builder.length() > 0) builder.setLength(builder.length() - 1);
    }
    return builder.toString();
  }

  private Flags() {}
}
