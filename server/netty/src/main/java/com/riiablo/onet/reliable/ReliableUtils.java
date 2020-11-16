package com.riiablo.onet.reliable;

public class ReliableUtils {
  private ReliableUtils() {}

  public static boolean sequenceGreaterThan(int s1, int s2) {
    return ((s1 > s2) && (s1 - s2 <= Short.MAX_VALUE))
        || ((s1 < s2) && (s2 - s1 >  Short.MAX_VALUE));
  }

  public static boolean sequenceLessThan(int s1, int s2) {
    return sequenceGreaterThan(s2, s1);
  }
}
