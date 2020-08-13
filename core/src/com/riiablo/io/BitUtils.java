package com.riiablo.io;

import java.util.Arrays;
import org.apache.logging.log4j.Logger;

import com.riiablo.util.DebugUtils;

public class BitUtils {
  private BitUtils() {}

  public static boolean isUnsigned(long value, int bits) {
    assert 0 < bits : "bits(" + bits + ") < " + 0;
    assert bits <= Long.SIZE : "bits(" + bits + ") > " + Long.SIZE;
    return (value & (1 << (bits - 1))) == 0;
  }

  public static boolean isUnsigned(byte value) {
    return isUnsigned(value, Byte.SIZE);
  }

  public static boolean isUnsigned(short value) {
    return isUnsigned(value, Short.SIZE);
  }

  public static boolean isUnsigned(int value) {
    return isUnsigned(value, Integer.SIZE);
  }

  public static boolean isUnsigned(long value) {
    return isUnsigned(value, Long.SIZE);
  }

  public static boolean readSignature(ByteInput in, final byte[] SIGNATURE, Logger log, String tag) {
    log.trace("Validating " + tag + " signature");
    if (in.bytesRemaining() < SIGNATURE.length) {
      byte[] signature = in.readBytes(in.bytesRemaining());
      throw new InvalidFormat(
          in,
          String.format(tag + " signature doesn't match expected signature: %s, expected %s",
          DebugUtils.toByteArray(signature),
          DebugUtils.toByteArray(SIGNATURE)));
    }

    byte[] signature = in.readBytes(SIGNATURE.length);
    boolean matched = Arrays.equals(signature, SIGNATURE);
    if (!matched) {
      throw new InvalidFormat(
          in,
          String.format(tag + " signature doesn't match expected signature: %s, expected %s",
          DebugUtils.toByteArray(signature),
          DebugUtils.toByteArray(SIGNATURE)));
    }
    return matched;
  }
}
