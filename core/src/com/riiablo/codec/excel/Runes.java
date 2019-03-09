package com.riiablo.codec.excel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntMap;

import com.riiablo.codec.excel.Excel;

public class Runes extends Excel<Runes.Entry> {
  private static final String TAG = "Runes";
  private static final boolean DEBUG = !true;

  @Override
  protected int offset() {
    return 26;
  }

  @Override
  protected void init() {
    /**
     * Runeword95 requires some custom code because there is a duplicate, and the indexing method
     * will not ignore the duplicate, but my code does because the hashmap will only return one of
     * the values. The map will not overwrite the subsequent duplicates, which isn't a problem for
     * at least this case, but duplicates should be indexed incrementally.
     *
     * Additionally, due to current method of iteration, 170 is max runeword ID -- Runeword170, but
     * this is more than the actual number of runewords in the list, so must be manually punched in.
     *
     * Long story short, below works fine, but it's some rocky code that any internal modifications
     * will seriously fuck up. Actually fixing it will require some API changes to how excel works.
     * Also, none of this solves delirium which is marked as ID 2718 and not 48
     */
    IntMap<Entry> remap = new IntMap<>();
    for (int i = 1, j = offset() + 1; i < 171; i++) {
      Entry entry = get("Runeword" + i);
      if (entry != null) {
        switch (i) {
          case 22: // Delirium
            if (DEBUG) Gdx.app.debug(TAG, entry + ":" + i + ":" + j);
            remap.put(2718, entry);
            j++;
            break;

          case 95: // Passion / Patience duplicate
            if (DEBUG) Gdx.app.debug(TAG, entry + ":" + i + ":" + j);
            remap.put(j, entry);
            j += 2;
            break;

          default:
            if (DEBUG) Gdx.app.debug(TAG, entry + ":" + i + ":" + j);
            remap.put(j, entry);
            j++;
        }
      } else {
        if (DEBUG) Gdx.app.debug(TAG, "skipping " + i + ":" + j);
      }
    }

    entries = remap;
    //id = offset() + NumberUtils.toInt(value.Name.substring(8), 0);
    //if (DEBUG) Gdx.app.debug(TAG, value + ":" + value.Name.substring(8) + ":" + id);
  }

  public static class Entry extends Excel.Entry {
    @Override
    public String toString() {
      return Rune_Name;
    }

    @Key
    @Column public String  Name;
    @Column(format = "Rune Name")
    public String  Rune_Name;
    @Column public boolean complete;
    @Column public boolean server;
    @Column(startIndex = 1, endIndex = 7)
    public String  itype[];
    @Column(startIndex = 1, endIndex = 4)
    public String  etype[];
    @Column(format = "*runes")
    public String  _runes;
    @Column(startIndex = 1, endIndex = 7)
    public String  Rune[];
    @Column(startIndex = 1, endIndex = 8)
    public String  T1Code[];
    @Column(startIndex = 1, endIndex = 8)
    public String  T1Param[];
    @Column(startIndex = 1, endIndex = 8)
    public String  T1Min[];
    @Column(startIndex = 1, endIndex = 8)
    public String  T1Max[];
  }
}
