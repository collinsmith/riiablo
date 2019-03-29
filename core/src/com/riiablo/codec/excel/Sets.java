package com.riiablo.codec.excel;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

public class Sets extends Excel<Sets.Entry> {
  private final IntMap<Array<SetItems.Entry>> ITEMS = new IntMap<>();

  public void index(SetItems items) {
    assert ITEMS.size == 0 : "Illegal state -- ITEM has already been indexed";
    for (SetItems.Entry item : items) {
      int id = index(item.set);
      Sets.Entry entry = item.parentSet = get(id);
      Array<SetItems.Entry> setItems = ITEMS.get(id);
      if (setItems == null) ITEMS.put(id, setItems = entry.items = new Array<>(6));
      setItems.add(item);
    }
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return index;
    }

    public Array<SetItems.Entry> getItems() {
      return items;
    }

    Array<SetItems.Entry> items;

    @Key
    @Column
    public String  index;
    @Column public String  name;
    @Column public int     version;
    @Column public int     level;
    @Column(format = "PCode2%s", endIndex = 2, values = {"a", "b"})
    public String  PCode2[];
    @Column(format = "PParam2%s", endIndex = 2, values = {"a", "b"})
    public int     PParam2[];
    @Column(format = "PMin2%s", endIndex = 2, values = {"a", "b"})
    public int     PMin2[];
    @Column(format = "PMax2%s", endIndex = 2, values = {"a", "b"})
    public int     PMax2[];
    @Column(format = "PCode3%s", endIndex = 2, values = {"a", "b"})
    public int     PCode3[];
    @Column(format = "PParam3%s", endIndex = 2, values = {"a", "b"})
    public int     PParam3[];
    @Column(format = "PMin3%s", endIndex = 2, values = {"a", "b"})
    public int     PMin3[];
    @Column(format = "PMax3%s", endIndex = 2, values = {"a", "b"})
    public int     PMax3[];
    @Column(format = "PCode4%s", endIndex = 2, values = {"a", "b"})
    public int     PCode4[];
    @Column(format = "PParam4%s", endIndex = 2, values = {"a", "b"})
    public int     PParam4[];
    @Column(format = "PMin4%s", endIndex = 2, values = {"a", "b"})
    public int     PMin4[];
    @Column(format = "PMax4%s", endIndex = 2, values = {"a", "b"})
    public int     PMax4[];
    @Column(format = "PCode5%s", endIndex = 2, values = {"a", "b"})
    public int     PCode5[];
    @Column(format = "PParam5%s", endIndex = 2, values = {"a", "b"})
    public int     PParam5[];
    @Column(format = "PMin5%s", endIndex = 2, values = {"a", "b"})
    public int     PMin5[];
    @Column(format = "PMax5%s", endIndex = 2, values = {"a", "b"})
    public int     PMax5[];
    @Column(startIndex = 1, endIndex = 9)
    public String  FCode[];
    @Column(startIndex = 1, endIndex = 9)
    public int     FParam[];
    @Column(startIndex = 1, endIndex = 9)
    public int     FMin[];
    @Column(startIndex = 1, endIndex = 9)
    public int     FMax[];
  }
}
