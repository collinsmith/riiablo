package com.riiablo.attributes;

public final class StatListBuilder {
  final StatList stats;
  final int list;

  StatListBuilder(StatList stats, int list) {
    this.stats = stats;
    this.list = list;
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

  /** @see StatList#put(int, short, int, int) */
  public StatListBuilder put(short stat, int param, int value) {
    stats.put(list, stat, param, value);
    return this;
  }

  /** @see StatList#put(int, short, int) */
  public StatListBuilder put(short stat, int value) {
    stats.put(list, stat, value);
    return this;
  }

  /** @see StatList#put(int, short, long) */
  public StatListBuilder put(short stat, long value) {
    stats.put(list, stat, value);
    return this;
  }

  /** @see StatList#put(int, short, float) */
  public StatListBuilder put(short stat, float value) {
    stats.put(list, stat, value);
    return this;
  }
}
