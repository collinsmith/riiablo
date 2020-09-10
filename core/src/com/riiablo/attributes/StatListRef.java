package com.riiablo.attributes;

import java.util.Iterator;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class StatListRef implements Iterable<StatRef> {
  private static final Logger log = LogManager.getLogger(StatListRef.class);

  final StatRef tuple;
  final StatList stats;
  final int list;

  StatListRef(final StatList stats, final int list) {
    this.stats = stats;
    this.list = list;
    this.tuple = new StatRef(stats, list);
  }

  StatList parent() {
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

  public boolean contains(final StatRef src) {
    return stats.contains(list, src);
  }

  int indexOf(final short stat) {
    return stats.indexOf(list, stat);
  }

  int indexOfEncoded(final short stat, final int encodedParams) {
    return stats.indexOfEncoded(list, stat, encodedParams);
  }

  int indexOf(final StatRef src) {
    return stats.indexOf(list, src);
  }

  StatRef get(final int index) {
    assert stats.contains(list, index);
    return tuple.update(index);
  }

  public StatRef get(final short stat) {
    final int index = indexOf(stat);
    if (index < 0) return null;
    return tuple.update(index);
  }

  public StatRef get(final short stat, final int encodedParams) {
    final int index = indexOfEncoded(stat, encodedParams);
    if (index < 0) return null;
    return tuple.update(index);
  }

  public StatRef get(final StatRef src) {
    final int index = indexOf(src);
    if (index < 0) return null;
    return tuple.update(index);
  }

  StatRef first(final short stat) {
    final int index = stats.firstIndexOf(list, stat);
    if (index < 0) return null;
    return tuple.update(index);
  }

  StatRef last() {
    return tuple; // TODO: validate index / state -- must be called after an operation is performed
  }

  public int getEncodedValue(final short stat, final int defaultEncodedValue) {
    final int index = indexOf(stat);
    if (index < 0) return defaultEncodedValue;
    return get(index).encodedValues();
  }

  public int getValue(final short stat, final int defaultValue) {
    final int index = indexOf(stat);
    if (index < 0) return Stat.encode(stat, defaultValue);
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

  public StatRef putEncoded(final short stat, final int encodedParams, final int encodedValues) {
    final int index = stats.putEncoded(list, stat, encodedParams, encodedValues);
    return tuple.update(index);
  }

  public StatRef putEncoded(final short stat, final int encodedValues) {
    final int index = stats.putEncoded(list, stat, encodedValues);
    return tuple.update(index);
  }

  public StatRef put(final short stat, final int value) {
    final int index = stats.put(list, stat, value);
    return tuple.update(index);
  }

  public StatRef put(final short stat, final long value) {
    final int index = stats.put(list, stat, value);
    return tuple.update(index);
  }

  public StatRef put(final short stat, final float value) {
    final int index = stats.put(list, stat, value);
    return tuple.update(index);
  }

  public StatRef addEncoded(final short stat, final int encodedValues) {
    if (containsAny(stat)) {
      final int index = stats.addEncoded(list, indexOf(stat), encodedValues);
      return tuple.update(index);
    } else {
      return putEncoded(stat, encodedValues);
    }
  }

  public StatRef add(final short stat, final int value) {
    if (containsAny(stat)) {
      final int index = stats.add(list, indexOf(stat), value);
      return tuple.update(index);
    } else {
      return put(stat, value);
    }
  }

  public StatRef add(final short stat, final long value) {
    if (containsAny(stat)) {
      final int index = stats.add(list, indexOf(stat), value);
      return tuple.update(index);
    } else {
      return put(stat, value);
    }
  }

  public StatRef add(final short stat, final float value) {
    if (containsAny(stat)) {
      final int index = stats.add(list, indexOf(stat), value);
      return tuple.update(index);
    } else {
      return put(stat, value);
    }
  }

  public StatRef add(final StatRef src) {
    if (contains(src)) {
      final int index = stats.addEncoded(list, indexOf(src), src.encodedValues());
      return tuple.update(index);
    } else {
      return putEncoded(src.id(), src.encodedParams(), src.encodedValues());
    }
  }

  public StatListRef addAll(final StatListRef src) {
    int index = tuple.index;
    for (StatRef stat : src) {
      if (contains(stat)) {
        index = stats.addEncoded(list, indexOf(stat), stat.encodedValues());
      } else {
        index = stats.putEncoded(list, stat.id(), stat.encodedParams(), stat.encodedValues());
      }
    }

    tuple.update(index);
    return this;
  }

  public StatListRef setAll(final StatListRef src) {
    stats.setAll(list, src);
    return this;
  }

  public StatList.IndexIterator indexIterator() {
    return stats.indexIterator(list);
  }

  public StatList.StatIterator statIterator() {
    return stats.statIterator(list);
  }

  @Override
  public Iterator<StatRef> iterator() {
    return statIterator();
  }

  public String debugString() {
    return stats.listDebugString(list);
  }
}
