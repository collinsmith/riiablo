package com.riiablo.attributes;

import com.riiablo.codec.excel.ItemStatCost;

public final class StatRef {
  public static StatRef obtain() {
    return new StatRef();
  }

  StatList stats;
  int list;
  int index;

  StatRef() {}

  StatRef(final StatList stats) {
    this(stats, 0, 0);
  }

  StatRef(final StatList stats, final int list) {
    this(stats, list, 0);
  }

  StatRef(final StatList stats, final int list, final int index) {
    this.stats = stats;
    this.list = list;
    this.index = index;
  }

  StatRef reset(final int list) {
    assert stats != null;
    this.list = list;
    return this;
  }

  StatRef update(final StatList stats, final int list, final int index) {
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

  public StatRef copy() {
    return new StatRef(stats, list, index);
  }

  public short id() {
    return stats.id(index);
  }

  public ItemStatCost.Entry entry() {
    return stats.entry(index);
  }

  public boolean modified() {
    return stats.modified(index);
  }

  public void forceUnmodified() {
    stats.forceUnmodified(index);
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

  public void set(final StatRef src) {
    stats.set(list, index, src);
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

  public void subEncoded(final int encodedValues) {
    stats.subEncoded(list, index, encodedValues);
  }

  public void sub(final int value) {
    stats.sub(list, index, value);
  }

  public void sub(final long value) {
    stats.sub(list, index, value);
  }

  public void sub(final float value) {
    stats.sub(list, index, value);
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
