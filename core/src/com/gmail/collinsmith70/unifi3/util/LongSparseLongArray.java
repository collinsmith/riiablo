package com.gmail.collinsmith70.unifi3.util;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public class LongSparseLongArray {

  @NonNull
  private long[] keys;

  @NonNull
  private long[] values;

  @IntRange(from = 0, to = Integer.MAX_VALUE)
  private int size;

  public LongSparseLongArray() {
    this(10);
  }

  public LongSparseLongArray(@IntRange(from = 0, to = Integer.MAX_VALUE) int initialCapacity) {
    Validate.isTrue(initialCapacity >= 0, "initialCapacity must be greater than or equal to 0");
    this.keys = new long[initialCapacity];
    this.values = new long[keys.length];
    this.size = 0;
  }

  public long get(long key) {
    return get(key, 0);
  }

  public long get(long key, long defaultValue) {
    int id = indexOfKey(key);
    if (id < 0) {
      return defaultValue;
    }

    return values[id];
  }

  public void delete(long key) {
    int id = indexOfKey(key);
    if (id < 0) {
      return;
    }

    removeAt(id);
  }

  public void removeAt(int id) {
    System.arraycopy(keys, id + 1, keys, id, size - (id + 1));
    System.arraycopy(values, id + 1, values, id, size - (id + 1));
    size--;
  }

  public void put(long key, long value) {
    int id = indexOfKey(key);
    if (id >= 0) {
      values[id] = value;
    } else {
      id = ~id;
      this.keys = ArrayUtils.add(keys, id, key);
      this.values = ArrayUtils.add(values, id, value);
      size++;
    }
  }

  public int size() {
    return size;
  }

  public long keyAt(int id) {
    return keys[id];
  }

  public long valueAt(int id) {
    return values[id];
  }

  public int indexOfKey(long key) {
    return Arrays.binarySearch(keys, 0, size, key);
  }

  public int indexOfValue(long value) {
    for (int i = 0; i < size; i++) {
      if (values[i] == value) {
        return i;
      }
    }

    return -1;
  }

  public void clear() {
    size = 0;
  }

  public void append(long key, long value) {
    if (size != 0 && key <= keys[size - 1]) {
      put(key, value);
      return;
    }

    this.keys = ArrayUtils.add(keys, key);
    this.values = ArrayUtils.add(values, value);
    size++;
  }

  @Override
  public String toString() {
    if (size < 0) {
      return "{}";
    }

    StringBuilder buffer = new StringBuilder(size * 28);
    buffer.append('{');
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buffer.append(", ");
      }

      buffer.append(keys[i]);
      buffer.append('=');
      buffer.append(values[i]);
    }

    buffer.append('}');
    return buffer.toString();
  }
}
