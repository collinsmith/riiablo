package com.riiablo.attributes;

import java.util.Iterator;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class StatListRef implements Iterable<com.riiablo.attributes.StatRef> {
  private static final Logger log = LogManager.getLogger(StatListRef.class);

  final com.riiablo.attributes.StatRef tuple;
  final com.riiablo.attributes.StatList stats;
  final int list;

  StatListRef(final com.riiablo.attributes.StatList stats, final int list) {
    this.stats = stats;
    this.list = list;
    this.tuple = new com.riiablo.attributes.StatRef(stats, list);
  }

  com.riiablo.attributes.StatList parent() {
    return stats;
  }

  public StatListRef clear() {
    stats.clear(list);
    return this;
  }

  public int listIndex() {
    return list;
  }

  public int size() {
    return stats.size(list);
  }

  public boolean isEmpty() {
    return stats.isEmpty(list);
  }

  boolean contains(final int index) {
    return stats.contains(list, index);
  }

  public boolean containsAny(final short stat) {
    return stats.containsAny(list, stat);
  }

  public boolean containsEncoded(final short stat, final int encodedParams) {
    return stats.containsEncoded(list, stat, encodedParams);
  }

  public boolean contains(final com.riiablo.attributes.StatRef src) {
    return stats.contains(list, src);
  }

  int indexOf(final short stat) {
    return stats.indexOf(list, stat);
  }

  int indexOfEncoded(final short stat, final int encodedParams) {
    return stats.indexOfEncoded(list, stat, encodedParams);
  }

  int indexOf(final com.riiablo.attributes.StatRef src) {
    return stats.indexOf(list, src);
  }

  com.riiablo.attributes.StatRef get(final int index) {
    assert stats.contains(list, index);
    return tuple.update(index);
  }

  public com.riiablo.attributes.StatRef get(final short stat) {
    final int index = indexOf(stat);
    if (index < 0) return null;
    return tuple.update(index);
  }

  public com.riiablo.attributes.StatRef get(final short stat, final int encodedParams) {
    final int index = indexOfEncoded(stat, encodedParams);
    if (index < 0) return null;
    return tuple.update(index);
  }

  public com.riiablo.attributes.StatRef get(final com.riiablo.attributes.StatRef src) {
    final int index = indexOf(src);
    if (index < 0) return null;
    return tuple.update(index);
  }

  com.riiablo.attributes.StatRef first(final short stat) {
    final int index = stats.firstIndexOf(list, stat);
    if (index < 0) return null;
    return tuple.update(index);
  }

  com.riiablo.attributes.StatRef last() {
    return tuple; // TODO: validate index / state -- must be called after an operation is performed
  }

  public int getEncodedValue(final short stat, final int defaultEncodedValue) {
    final int index = indexOf(stat);
    if (index < 0) return defaultEncodedValue;
    return get(index).encodedValues();
  }

  public int getValue(final short stat, final int defaultValue) {
    final int index = indexOf(stat);
    if (index < 0) return com.riiablo.attributes.Stat.encode(stat, defaultValue);
    return get(index).asInt();
  }

  public long getValue(final short stat, final long defaultValue) {
    final int index = indexOf(stat);
    if (index < 0) return defaultValue;
    return get(index).asLong();
  }

  public float getValue(final short stat, final float defaultValue) {
    final int index = indexOf(stat);
    if (index < 0) return defaultValue;
    return get(index).asFixed();
  }

  public com.riiablo.attributes.StatRef putEncoded(final short stat, final int encodedParams, final int encodedValues) {
    final int index = stats.putEncoded(list, stat, encodedParams, encodedValues);
    return tuple.update(index);
  }

  public com.riiablo.attributes.StatRef putEncoded(final short stat, final int encodedValues) {
    final int index = stats.putEncoded(list, stat, encodedValues);
    return tuple.update(index);
  }

  public com.riiablo.attributes.StatRef put(final short stat, final int value) {
    final int index = stats.put(list, stat, value);
    return tuple.update(index);
  }

  public com.riiablo.attributes.StatRef put(final short stat, final long value) {
    final int index = stats.put(list, stat, value);
    return tuple.update(index);
  }

  public com.riiablo.attributes.StatRef put(final short stat, final float value) {
    final int index = stats.put(list, stat, value);
    return tuple.update(index);
  }

  public com.riiablo.attributes.StatRef addEncoded(final short stat, final int encodedValues) {
    if (containsAny(stat)) {
      final int index = stats.addEncoded(list, indexOf(stat), encodedValues);
      return tuple.update(index);
    } else {
      return putEncoded(stat, encodedValues);
    }
  }

  public com.riiablo.attributes.StatRef add(final short stat, final int value) {
    if (containsAny(stat)) {
      final int index = stats.add(list, indexOf(stat), value);
      return tuple.update(index);
    } else {
      return put(stat, value);
    }
  }

  public com.riiablo.attributes.StatRef add(final short stat, final long value) {
    if (containsAny(stat)) {
      final int index = stats.add(list, indexOf(stat), value);
      return tuple.update(index);
    } else {
      return put(stat, value);
    }
  }

  public com.riiablo.attributes.StatRef add(final short stat, final float value) {
    if (containsAny(stat)) {
      final int index = stats.add(list, indexOf(stat), value);
      return tuple.update(index);
    } else {
      return put(stat, value);
    }
  }

  public com.riiablo.attributes.StatRef add(final com.riiablo.attributes.StatRef src) {
    if (contains(src)) {
      final int index = stats.addEncoded(list, indexOf(src), src.encodedValues());
      return tuple.update(index);
    } else {
      return putEncoded(src.id(), src.encodedParams(), src.encodedValues());
    }
  }

  public StatListRef setAll(final StatListRef src) {
    stats.setAll(list, src);
    return this;
  }

  public com.riiablo.attributes.StatList.IndexIterator indexIterator() {
    return stats.indexIterator(list);
  }

  public com.riiablo.attributes.StatList.StatIterator statIterator() {
    return stats.statIterator(list);
  }

  @Override
  public Iterator<com.riiablo.attributes.StatRef> iterator() {
    return statIterator();
  }
}
