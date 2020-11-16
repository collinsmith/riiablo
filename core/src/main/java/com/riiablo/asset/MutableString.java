package com.riiablo.asset;

import io.netty.util.AsciiString;
import io.netty.util.HashingStrategy;

public final class MutableString implements CharSequence {
  static final HashingStrategy<CharSequence> HASHING_STRATEGY = AsciiString.CASE_INSENSITIVE_HASHER;

  public static MutableString wrap(CharSequence charSequence) {
    return new MutableString(charSequence);
  }

  AsciiString string;

  MutableString(CharSequence charSequence) {
    string = new AsciiString(charSequence);
  }

  public MutableString replace(char oldChar, char newChar) {
    string.replace(oldChar, newChar);
    return this;
  }

  public MutableString transform(final byte[] transform) {
    boolean changed = false;
    final byte[] array = string.array();
    for (int i = string.arrayOffset(), s = i + string.length(); i < s; i++) {
      final byte b = array[i];
      changed |= b != (array[i] = transform[b]);
    }

    if (changed) invalidate();
    return this;
  }

  public MutableString invalidate() {
    string.arrayChanged();
    return this;
  }

  @Override
  public int length() {
    return string.length();
  }

  @Override
  public char charAt(int index) {
    return string.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return string.subSequence(start, end);
  }

  @Override
  public int hashCode() {
    return HASHING_STRATEGY.hashCode(string);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof MutableString)) {
      return false;
    }

    MutableString other = (MutableString) obj;
    return HASHING_STRATEGY.equals(string, other.string);
  }

  @Override
  public String toString() {
    return string.toString();
  }
}
