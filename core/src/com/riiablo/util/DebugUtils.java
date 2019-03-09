package com.riiablo.util;

public class DebugUtils {
  private DebugUtils() {}

  public static String toByteArray(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (byte b : bytes) sb.append(String.format("%02X", b & 0xFF)).append(", ");
    if (sb.length() > 1) sb.setLength(sb.length() - 2);
    sb.append("]");
    return sb.toString();
  }
}
