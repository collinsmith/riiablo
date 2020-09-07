package com.riiablo.attributes;

import com.riiablo.codec.excel.ItemStatCost;

public final class StatGetter {
  StatList stats;
  int index;

  StatGetter() {}

  StatGetter(StatList stats) {
    this.stats = stats;
  }

  StatGetter set(StatList stats, int index) {
    this.stats = stats;
    this.index = index;
    return this;
  }

  StatGetter update(int index) {
    assert stats != null;
    this.index = index;
    return this;
  }

  /** @see StatList#param(int) */
  int param() {
    return stats.param(index);
  }

  /** @see StatList#param(int) */
  public int params() { // different name to avoid #param() misuse
    return param();
  }

  /** @see StatList#value(int) */
  int value() {
    return stats.value(index);
  }

  public StatGetter copy() {
    return new StatGetter().set(stats, index);
  }

  /** @see StatList#id(int) */
  public short id() {
    return stats.id(index);
  }

  /** @see StatList#entry(int) */
  public ItemStatCost.Entry entry() {
    return stats.entry(index);
  }

  /** @see StatList#asInt(int) */
  public int asInt() {
    return stats.asInt(index);
  }

  /** @see StatList#asLong(int) */
  public long asLong() {
    return stats.asLong(index);
  }

  /** @see StatList#asFixed(int) */
  public float asFixed() {
    return stats.asFixed(index);
  }

  /** @see StatList#asString(int) */
  public String asString() {
    return stats.asString(index);
  }

  /** @see StatList#indexDebugString(int) */
  public String debugString() {
    return stats.indexDebugString(index);
  }

  @Override
  public String toString() {
    return asString();
  }

  /** @see StatList#value1(int) */
  public int value1() {
    return stats.value1(index);
  }

  /** @see StatList#value2(int) */
  public int value2() {
    return stats.value2(index);
  }

  /** @see StatList#value3(int) */
  public int value3() {
    return stats.value3(index);
  }

  /** @see StatList#param1(int) */
  public int param1() {
    return stats.param1(index);
  }

  /** @see StatList#param2(int) */
  public int param2() {
    return stats.param2(index);
  }

  /** @see StatList#add(int, int) */
  public void add(int value) {
    stats.add(index, value);
  }

  /** @see StatList#add(int, long) */
  public void add(long value) {
    stats.add(index, value);
  }

  /** @see StatList#add(int, float) */
  public void add(float value) {
    stats.add(index, value);
  }

  public void addShifted(int value) {
    add(value << entry().ValShift);
  }
}
