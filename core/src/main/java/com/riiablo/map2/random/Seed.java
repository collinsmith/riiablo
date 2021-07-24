package com.riiablo.map2.random;

import org.apache.commons.lang3.Conversion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Base64Coder;

public final class Seed {
  public static Seed random() {
    return from(
        MathUtils.random.nextLong(),
        MathUtils.random.nextLong());
  }

  public static Seed fixed() {
    return from(0xDEADBEEFL, 0x1337C0D3L); // TODO: replace with something better
  }

  public static Seed from(long seed0, long seed1) {
    return new Seed(seed0, seed1);
  }

  public static Seed decode(String base64) {
    byte[] bytes = Base64Coder.decode(StringUtils.rightPad(base64, 24, '='));
    long seed0 = Conversion.byteArrayToLong(bytes, 0, 0L, 0, 8);
    long seed1 = Conversion.byteArrayToLong(bytes, 8, 0L, 0, 8);
    return new Seed(seed0, seed1);
  }

  public static String encode(long seed0, long seed1) {
    byte[] bytes = new byte[16]; // 2 longs
    Conversion.longToByteArray(seed0, 0, bytes, 0, 8);
    Conversion.longToByteArray(seed1, 0, bytes, 8, 8);
    char[] chars = Base64Coder.encode(bytes);
    return new String(chars); // trim padding
  }

  final long seed0;
  final long seed1;

  Seed(long seed0, long seed1) {
    this.seed0 = seed0;
    this.seed1 = seed1;
  }

  public String encode() {
    return encode(seed0, seed1);
  }

  static String toString(long seed) {
    return String.format("0x%016x", seed);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("seed0", toString(seed0))
        .append("seed1", toString(seed1))
        .toString();
  }
}
