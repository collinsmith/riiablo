package com.riiablo.attributes;

import java.util.Iterator;

import com.riiablo.logger.LogManager;
import com.riiablo.logger.Logger;

public final class StatListGetter implements Iterable<StatGetter> {
  private static final Logger log = LogManager.getLogger(StatListGetter.class);

  final StatList stats;
  final int list;
  final StatGetter tuple = new StatGetter();

  StatListGetter(StatList stats, int list) {
    this.stats = stats;
    this.list = list;
  }

  /** @see StatList#param(int) */
  int param(short stat) {
    return stats.param(indexOf(stat));
  }

  /** @see StatList#value(int) */
  int value(short stat) {
    return stats.value(indexOf(stat));
  }

  /** @see StatList#indexOf(int, short) */
  int indexOf(short stat) {
    return stats.indexOf(list, stat);
  }

  StatList parent() {
    return stats;
  }

  public StatListBuilder builder() {
    return new StatListBuilder(stats, list);
  }

  /** @see StatList#clearList(int) */
  public StatListGetter clear() {
    stats.clearList(list);
    return this;
  }

  /** @see StatList#size(int) */
  public int size() {
    return stats.size(list);
  }

  /** @see StatList#isEmpty(int) */
  public boolean isEmpty() {
    return stats.isEmpty(list);
  }

  /** @see StatList#contains(int, short) */
  public boolean contains(short stat) {
    return stats.contains(list, stat);
  }

  /** @see StatList#contains(int, short) */
  public boolean contains(StatGetter stat) {
    return stats.contains(list, stat.id(), stat.param());
  }

  /** @see StatList#get(int) */
  public StatGetter get(int index) {
    // TODO: enforce index in [list.startIndex..list.endIndex)
    return tuple.set(stats, index);
  }

  /** @see StatList#indexOf(int, short) */
  public StatGetter get(short stat) {
    final int index = indexOf(stat);
    if (index < 0) return null;
    return tuple.set(stats, index);
  }

  /** @see #get(short) */
  public int getValue(short stat, int defaultValue) {
    final int index = indexOf(stat);
    return index >= 0 ? tuple.set(stats, index).value() : defaultValue;
  }

  /** @see StatList#indexOf(int, short, int) */
  public StatGetter get(StatGetter stat) {
    final int index = stats.indexOf(list, stat.id(), stat.param());
    if (index < 0) {
      log.warn("stats({}) list({}) did not contain stat({})", stats, list, stat);
      return null;
    }

    return tuple.set(stats, index);
  }

  /** @see StatList#firstIndexOf(int, short) */
  StatGetter first(short stat) {
    final int index = stats.firstIndexOf(list, stat);
    if (index < 0) return null;
    return tuple.set(stats, index);
  }

  /** @see StatList#indexIterator(int) */
  public StatList.IndexIterator indexIterator() {
    return stats.indexIterator(list);
  }

  /** @see StatList#statIterator(int) */
  public StatList.StatIterator statIterator() {
    return stats.statIterator(list);
  }

  /** @see #statIterator() */
  @Override
  public Iterator<StatGetter> iterator() {
    return statIterator();
  }

  /** @see StatList#listDebugString(int) */
  public String debugString() {
    return stats.listDebugString(list);
  }

  @Override
  public String toString() {
    return debugString();
  }
}
