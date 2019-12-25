package com.riiablo.codec.excel;

import java.util.Arrays;

@Excel.Binned
public class MonPreset extends Excel<MonPreset.Entry> {
  private static final int MAX_ACTS = 5;
  private static final int MAX_ENTRIES = 64;

  private final int[][] lookup = new int[MAX_ACTS + 1][MAX_ENTRIES];
  private final int[] index = new int[MAX_ACTS + 1];

  @Override
  protected void put(int id, Entry value) {
    super.put(id, value);
    int act = value.Act;
    lookup[act][index[act]++] = id;
  }

  @Override
  protected void init() {
    for (int act = 1; act <= MAX_ACTS; act++) {
      int[] lookup = this.lookup[act];
      Arrays.fill(lookup, index[act], lookup.length, -1);
    }
  }

  public Entry get(int act, int id) {
    return get(lookup[act][id]);
  }

  public String getPlace(int act, int id) {
    return get(act, id).Place;
  }

  public int getSize(int act) {
    return index[act];
  }

  @Excel.Index
  public static class Entry extends Excel.Entry {
    @Column public int     Act;
    @Column public String  Place;
  }
}
