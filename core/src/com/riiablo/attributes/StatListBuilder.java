package com.riiablo.attributes;

public final class StatListBuilder {
  final StatList stats;
  final int list;
  final StatGetter tuple;
  int index;

  StatListBuilder(StatList stats, int list) {
    this.stats = stats;
    this.list = list;
    this.tuple = new StatGetter(stats);
    this.index = -1;
  }

  /**
   * Returns the list index.
   */
  public int build() {
    return list;
  }

  /** @see StatList#get(int) */
  public StatListGetter get() {
    return stats.get(list);
  }

  /** @see StatList#get(int) */
  public StatGetter last() {
    if (index < 0) throw new IllegalStateException("cannot retrieve last stat when no stats have been added yet!");
    return tuple.update(index);
  }

  /** @see StatList#put(int, short, int, int) */
  public StatListBuilder put(short stat, int param, int value) {
    index = stats.put(list, stat, param, value);
    return this;
  }

  /** @see StatList#put(int, short, int) */
  public StatListBuilder put(short stat, int value) {
    index = stats.put(list, stat, value);
    return this;
  }

  /** @see StatList#put(int, short, long) */
  public StatListBuilder put(short stat, long value) {
    index = stats.put(list, stat, value);
    return this;
  }

  /** @see StatList#put(int, short, float) */
  public StatListBuilder put(short stat, float value) {
    index = stats.put(list, stat, value);
    return this;
  }
}
