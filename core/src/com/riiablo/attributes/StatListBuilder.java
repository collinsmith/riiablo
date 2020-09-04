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
  public int index() {
    return list;
  }

  /**
   * Returns the list.
   */
  public StatList build() {
    return stats;
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

  /** @see StatList#put(int, short, int, int, int, int, int) */
  public StatListBuilder put(short stat, int param1, int param2, int value1, int value2, int value3) {
    index = stats.put(list, stat, param1, param2, value1, value2, value3);
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

  /** @see StatList#add(int, StatGetter) */
  public StatListBuilder add(StatGetter stat) {
    index = stats.add(list, stat);
    return this;
  }

  /** @see StatList#addAll(int, StatList, int) */
  public StatListBuilder addAll(StatListGetter src) {
    index = stats.addAll(list, src.stats, src.list);
    return this;
  }
}
