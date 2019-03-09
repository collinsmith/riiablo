package com.riiablo.mpq.virtual;

public class VirtualUtils {
  private VirtualUtils() {}

  public static String createPalettePath(String palette) {
    return "data/global/palette/" + palette + "/pal.dat";
  }
}
