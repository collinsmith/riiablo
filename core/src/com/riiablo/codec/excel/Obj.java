package com.riiablo.codec.excel;

import com.badlogic.gdx.utils.IntArray;

@Excel.Binned
public class Obj extends Excel<Obj.Entry> {
  private static final int MAX_ACTS = 5;
  private static final int INITIAL_ENTRIES = 150;

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

  public int getObjectId(int act, int id) {
    return get(act, id).ObjectId;
  }

  public int getSize(int act) {
    return lookup[act].size;
  }

  @Excel.Index
  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Description;
    }

    @Column public int     Act;
    @Column public int     Id;
    @Column public String  Description;
    @Column public int     ObjectId;
  }
}
