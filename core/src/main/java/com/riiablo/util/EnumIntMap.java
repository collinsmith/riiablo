package com.riiablo.util;

import com.badlogic.gdx.utils.Collections;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class EnumIntMap<K extends Enum<K>> implements Iterable<EnumIntMap.Entry<K>> {
  private final Class<K> keyType;
  private final int defaultValue;

  private K[] keyUniverse;
  private int[] vals;

  private Entries<K> entries1, entries2;

  public EnumIntMap(Class<K> keyType, int defaultValue) {
    this.keyType = keyType;
    this.defaultValue = defaultValue;
    keyUniverse = keyType.getEnumConstants();
    vals = new int[keyUniverse.length];
    Arrays.fill(vals, defaultValue);
  }

  public void clear() {
    Arrays.fill(vals, defaultValue);
  }

  public int get(K key) {
    return vals[key.ordinal()];
  }

  public int remove(K key) {
    return put(key, defaultValue);
  }

  public int put(K key, int value) {
    int i = key.ordinal();
    int val = vals[i];
    vals[i] = value;
    return val;
  }

  public int[] values() {
    return vals;
  }

  @Override
  public Entries<K> iterator() {
    return entries();
  }

  public Entries<K> entries () {
    if (Collections.allocateIterators) return new Entries<>(this);
    if (entries1 == null) {
      entries1 = new Entries<>(this);
      entries2 = new Entries<>(this);
    }
    if (!entries1.valid) {
      entries1.reset();
      entries1.valid = true;
      entries2.valid = false;
      return entries1;
    }
    entries2.reset();
    entries2.valid = true;
    entries1.valid = false;
    return entries2;
  }

  public static class Entry<K extends Enum<K>> {
    public K key;
    public int value;

    public String toString () {
      return key + "=" + value;
    }
  }

  private static class MapIterator<K extends Enum<K>> {
    public boolean hasNext;

    final EnumIntMap<K> map;
    int nextIndex, currentIndex;
    boolean valid = true;

    public MapIterator(EnumIntMap<K> map) {
      this.map = map;
      reset();
    }

    public void reset() {
      currentIndex = -1;
      nextIndex = -1;
      findNextIndex();
    }

    void findNextIndex() {
      hasNext = false;
      int[] vals = map.vals;
      int defaultValue = map.defaultValue;
      for (int n = map.keyUniverse.length; ++nextIndex < n;) {
        if (vals[nextIndex] != defaultValue) {
          hasNext = true;
          break;
        }
      }
    }

    public void remove() {
      throw new UnsupportedOperationException("#remove() not supported.");
    }
  }

  public static class Entries<K extends Enum<K>> extends EnumIntMap.MapIterator<K> implements Iterable<Entry<K>>, Iterator<Entry<K>> {
    private Entry<K> entry = new Entry<>();

    public Entries(EnumIntMap<K> map) {
      super(map);
    }

    /** Note the same entry instance is returned each time this method is called. */
    public Entry<K> next() {
      if (!hasNext) throw new NoSuchElementException();
      if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
      entry.key = map.keyUniverse[nextIndex];
      entry.value = map.vals[nextIndex];
      currentIndex = nextIndex;
      findNextIndex();
      return entry;
    }

    public boolean hasNext() {
      if (!valid) throw new GdxRuntimeException("#iterator() cannot be used nested.");
      return hasNext;
    }

    public Entries<K> iterator() {
      return this;
    }

    public void remove() {
      super.remove();
    }
  }
}
