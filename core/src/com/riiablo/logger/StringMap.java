package com.riiablo.logger;

import java.util.Arrays;
import java.util.Iterator;
import org.apache.commons.lang3.ArrayUtils;

import com.badlogic.gdx.utils.ObjectIntMap;

public class StringMap {
  private static final int DEFAULT_CAPACITY = 4;
  private final ObjectIntMap<String> indexes;
  private String[] keys;
  private String[] vals;
  private int size;

  private boolean immutable;
  private StringMap immutableCopy;
  private String toString;

  public StringMap() {
    keys = ArrayUtils.EMPTY_STRING_ARRAY;
    vals = ArrayUtils.EMPTY_STRING_ARRAY;
    indexes = new ObjectIntMap<>(DEFAULT_CAPACITY);
  }

  private StringMap(StringMap other) {
    assert other.keys.length == other.vals.length;
    assert other.size == other.indexes.size;
    size = other.size;
    keys = Arrays.copyOf(other.keys, size);
    vals = Arrays.copyOf(other.vals, size);
    indexes = new ObjectIntMap<>(other.indexes);
    immutable = true;
  }

  private void inflateTable(final int size) {
    assert !immutable;
    assert this.size == 0;
    keys = new String[size];
    vals = new String[size];
  }

  private void ensureCapacity(final int size) {
    assert !immutable;
    assert keys.length == vals.length;
    if (size >= this.size) {
      final int resize = Math.max(this.size * 2, DEFAULT_CAPACITY);
      assert resize > size;
      keys = Arrays.copyOf(keys, resize);
      vals = Arrays.copyOf(vals, resize);
    }
  }

  private void assertMutable() {
    if (immutable) {
      throw new UnsupportedOperationException("StringMap has been frozen.");
    }
  }

  public void put(String key, String value) {
    if (key == null) {
      throw new IllegalArgumentException("key == null");
    }
    if (value == null) {
      throw new IllegalArgumentException("value == null -- use remove instead.");
    }

    assertMutable();
    if (immutableCopy != null) {
      immutableCopy = null;
    }

    assert keys.length == vals.length;
    if (keys.length == 0) {
      inflateTable(DEFAULT_CAPACITY);
    }

    assert keys != null && vals != null;
    final int index = indexes.get(key, size);
    if (index < size) {
      vals[index] = value;
    } else {
      ensureCapacity(index);
      keys[index] = key;
      vals[index] = value;
      indexes.put(key, index);
      size++;
    }
    assert size == indexes.size;
    assert size > 0;
  }

  public String get(String key) {
    assert keys.length == vals.length;
    if (keys.length == 0) {
      return null;
    }

    final int index = indexes.get(key, -1);
    return index >= 0 ? vals[index] : null;
  }

  public void remove(String key) {
    assertMutable();
    if (immutableCopy != null) {
      immutableCopy = null;
    }

    assert keys.length == vals.length;
    if (keys.length == 0) {
      return;
    }

    final int index = indexes.get(key, -1);
    if (index < 0) return;
    System.arraycopy(keys, index + 1, keys, index, size - index - 1);
    System.arraycopy(vals, index + 1, vals, index, size - index - 1);
    keys[size] = vals[size] = null;
    indexes.remove(key, -1);
    size--;
    assert size == indexes.size;
  }

  public void clear() {
    assert keys != null || keys == vals;
    if (keys == null) {
      assert size == 0;
      return;
    }

    Arrays.fill(keys, null);
    Arrays.fill(vals, null);
    indexes.clear();
    size = 0;
    assert size == indexes.size;
  }

  public int size() {
    assert size == indexes.size;
    return size;
  }

  public boolean isEmpty() {
    assert size == indexes.size;
    return size == 0;
  }

  public StringMap freeze() {
    return immutableCopy == null
        ? immutableCopy = new StringMap(this)
        : immutableCopy;
  }

  @Override
  public String toString() {
    if (toString == null) {
      StringBuilder buffer = new StringBuilder(256);
      buffer.append('{');
      for (int i = 0, s = size; i < s; i++) {
        appendEntry(i, buffer).append(',');
      }

      if (size > 0) {
        buffer.setLength(buffer.length() - 1);
      }

      buffer.append('}');
      toString = buffer.toString();
    }

    return toString;
  }

  StringBuilder appendEntry(int index, StringBuilder buffer) {
    assert index >= 0 && index < size;
    return buffer.append(keys[index]).append('=').append(vals[index]);
  }

  private void assertImmutable() {
    if (!immutable) {
      throw new UnsupportedOperationException("StringMap has not been frozen.");
    }
  }

  public StringMapIterator iterator() {
    return iterator(0);
  }

  public StringMapIterator iterator(int startIndex) {
    if (startIndex < 0) {
      throw new IllegalArgumentException("startIndex(" + startIndex + ") < " + 0);
    }
    assertImmutable();
    return new StringMapIterator(startIndex);
  }

  public class StringMapIterator implements Iterator<Entry> {
    final Entry entry = new Entry();
    int index;

    StringMapIterator(int index) {
      assert immutable;
      this.index = index;
    }

    @Override
    public boolean hasNext() {
      return index < size;
    }

    @Override
    public Entry next() {
      entry.key = keys[index];
      entry.value = vals[index];
      index++;
      return entry;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public static class Entry {
    String key;
    String value;

    Entry() {}

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return key + "=" + value;
    }
  }
}
