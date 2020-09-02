package com.riiablo.attributes;

public final class StatListGetter {
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

  /** @see StatList#contains(int, short) */
  public boolean contains(short stat) {
    return stats.contains(list, stat);
  }

  /** @see StatList#indexOf(int, short) */
  public StatGetter get(short stat) {
    final int index = indexOf(stat);
    if (index < 0) return null;
    return tuple.set(stats, index);
  }

  /** @see StatList#firstIndexOf(int, short) */
  StatGetter first(short stat) {
    final int index = stats.firstIndexOf(list, stat);
    if (index < 0) return null;
    return tuple.set(stats, index);
  }

  public void addAll(StatListGetter src) {
    stats.addAll(list, src.stats, src.list);
  }

  /** @see StatList#indexIterator(int) */
  public StatList.IndexIterator indexIterator() {
    return stats.indexIterator(list);
  }

  /** @see StatList#statIterator(int) */
  public StatList.StatIterator statIterator() {
    return stats.statIterator(list);
  }

  /** @see StatList#listDebugString(int) */
  public String debugString() {
    return stats.listDebugString(list);
  }

  @Override
  public String toString() {
    return debugString();
  }

//  public int asInt(short stat) {
//    return stats.asInt(indexOf(stat));
//  }

//  public long asLong(short stat) {
//    return stats.asLong(indexOf(stat));
//  }

//  public float asFixed(short stat) {
//    return stats.asFixed(indexOf(stat));
//  }

//  public String asString(short stat) {
//    return stats.asString(indexOf(stat));
//  }

//  public int value1(short stat) {
//    return stats.value1(indexOf(stat));
//  }

//  public int value2(short stat) {
//    return stats.value2(indexOf(stat));
//  }

//  public int value3(short stat) {
//    return stats.value3(indexOf(stat));
//  }

//  public int param1(short stat) {
//    return stats.param1(indexOf(stat));
//  }

//  public int param2(short stat) {
//    return stats.param2(indexOf(stat));
//  }
}
