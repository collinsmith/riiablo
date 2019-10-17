package com.riiablo.engine;

public final class Dirty {
  public static final int NONE = 0;
  public static final int HD = 1 << 0;
  public static final int TR = 1 << 1;
  public static final int LG = 1 << 2;
  public static final int RA = 1 << 3;
  public static final int LA = 1 << 4;
  public static final int RH = 1 << 5;
  public static final int LH = 1 << 6;
  public static final int SH = 1 << 7;
  public static final int S1 = 1 << 8;
  public static final int S2 = 1 << 9;
  public static final int S3 = 1 << 10;
  public static final int S4 = 1 << 11;
  public static final int S5 = 1 << 12;
  public static final int S6 = 1 << 13;
  public static final int S7 = 1 << 14;
  public static final int S8 = 1 << 15;
  public static final int ALL = 0xFFFF;

  public static String toString(int bits) {
    StringBuilder builder = new StringBuilder();
    if (bits == NONE) {
      builder.append("NONE");
    } else if (bits == ALL) {
      builder.append("ALL");
    } else {
      if ((bits & HD) == HD) builder.append("HD").append("|");
      if ((bits & TR) == TR) builder.append("TR").append("|");
      if ((bits & LG) == LG) builder.append("LG").append("|");
      if ((bits & RA) == RA) builder.append("RA").append("|");
      if ((bits & LA) == LA) builder.append("LA").append("|");
      if ((bits & RH) == RH) builder.append("RH").append("|");
      if ((bits & LH) == LH) builder.append("LH").append("|");
      if ((bits & SH) == SH) builder.append("SH").append("|");
      if ((bits & S1) == S1) builder.append("S1").append("|");
      if ((bits & S2) == S2) builder.append("S2").append("|");
      if ((bits & S3) == S3) builder.append("S3").append("|");
      if ((bits & S4) == S4) builder.append("S4").append("|");
      if ((bits & S5) == S5) builder.append("S5").append("|");
      if ((bits & S6) == S6) builder.append("S6").append("|");
      if ((bits & S7) == S7) builder.append("S7").append("|");
      if ((bits & S8) == S8) builder.append("S8").append("|");
      if (builder.length() > 0) builder.setLength(builder.length() - 1);
    }
    return builder.toString();
  }

  public static boolean isDirty(int flags, int component) {
    return ((1 << component) & flags) != 0;
  }
}
