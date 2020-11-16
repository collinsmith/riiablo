package com.riiablo.codec.excel;

import com.badlogic.gdx.utils.IntArray;

@Excel.Binned
public class MonPreset extends Excel<MonPreset.Entry> {
  private static final int MAX_ACTS = 5;
  private static final int INITIAL_ENTRIES = 60;

  private final IntArray[] lookup = new IntArray[MAX_ACTS + 1]; {
    for (int act = 1; act <= MAX_ACTS; act++) lookup[act] = new IntArray(INITIAL_ENTRIES);
  }

  @Override
  protected void put(int id, Entry value) {
    super.put(id, value);
    lookup[value.Act].add(id);
  }

  public Entry get(int act, int id) {
    return get(lookup[act].get(id));
  }

  public String getPlace(int act, int id) {
    return get(act, id).Place;
  }

  public int getSize(int act) {
    return lookup[act].size;
  }

  @Excel.Index
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Place;
    }

    @Column public int     Act;
    @Column public String  Place;
  }
}
