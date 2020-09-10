package com.riiablo.attributes;

import com.riiablo.codec.excel.ItemStatCost;

public final class StatRef {
  com.riiablo.attributes.StatList stats;
  int list;
  int index;

  StatRef() {}

  StatRef(final com.riiablo.attributes.StatList stats) {
    this.stats = stats;
  }

  StatRef(final com.riiablo.attributes.StatList stats, final int list) {
    this(stats);
    this.list = list;
  }

  StatRef reset(final int list) {
    assert stats != null;
    this.list = list;
    return this;
  }

  StatRef update(final com.riiablo.attributes.StatList stats, final int list, final int index) {
    this.stats = stats;
    this.list = list;
    this.index = index;
    return this;
  }

  StatRef update(final int index) {
    assert stats != null;
    this.index = index;
    return this;
  }

  public short id() {
    return stats.id(index);
  }

  public ItemStatCost.Entry entry() {
    return stats.entry(index);
  }

  public int encodedValues() {
    return stats.encodedValues(index);
  }

  public int encodedParams() {
    return stats.encodedParams(index);
  }

  public int value0() {
    return stats.value0(index);
  }

  public int value1() {
    return stats.value1(index);
  }

  public int value2() {
    return stats.value2(index);
  }

  public int param0() {
    return stats.param0(index);
  }

  public int param1() {
    return stats.param1(index);
  }

  public int asInt() {
    return stats.asInt(index);
  }

  public long asLong() {
    return stats.asLong(index);
  }

  public float asFixed() {
    return stats.asFixed(index);
  }

  public void setEncoded(final int encodedValues) {
    stats.setEncoded(list, index, encodedValues);
  }

  public void set(final int value) {
    stats.set(list, index, value);
  }

  public void set(final long value) {
    stats.set(list, index, value);
  }

  public void set(final float value) {
    stats.set(list, index, value);
  }

  public void addEncoded(final int encodedValues) {
    stats.addEncoded(list, index, encodedValues);
  }

  public void add(final int value) {
    stats.add(list, index, value);
  }

  public void add(final long value) {
    stats.add(list, index, value);
  }

  public void add(final float value) {
    stats.add(list, index, value);
  }

  public String asString() {
    return stats.asString(index);
  }

  public String debugString() {
    return stats.indexDebugString(index);
  }

  @Override
  public String toString() {
    return debugString();
  }
}
