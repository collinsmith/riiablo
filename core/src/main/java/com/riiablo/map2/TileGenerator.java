package com.riiablo.map2;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectSet;

import com.riiablo.map2.DT1.Tile;
import com.riiablo.map2.random.Random;

public class TileGenerator {
  final ObjectSet<DT1> dt1s = new ObjectSet<>();
  final IntMap<Entry> entries = new IntMap<>();

  final Random random = new Random();

  public void add(DT1 dt1) {
    dt1s.add(dt1);
    Tile[] tiles = dt1.tiles;
    for (int i = 0, s = dt1.numTiles; i < s; i++) {
      add(tiles[i]);
    }
  }

  void add(Tile tile) {
    // if (tile.rarity <= 0) return;
    Entry entry = entries.get(tile.tileIndex);
    if (entry == null) entries.put(tile.tileIndex, entry = new Entry());
    entry.add(tile);
  }

  public void dispose() {
    dt1s.clear();
    entries.clear();
  }

  public Tile next(int orientation, int mainIndex, int subIndex) {
    int id = Tile.Index.create(orientation, mainIndex, subIndex);
    return next(id);
  }

  public Tile next(int tileIndex) {
    Entry entry = entries.get(tileIndex);
    if (entry == null) return null;
    int sum = entry.probability;
    int chance = sum == 0 ? 0 : this.random.nextInt(sum);
    Tile[] items = entry.tiles.items;
    Tile tile = null;
    for (int i = 0, s = entry.tiles.size; i < s && chance > 0; i++) {
      tile = items[i];
      chance -= tile.rarity;
    }

    return tile;
  }

  static final class Entry {
    int probability;
    final Array<Tile> tiles = new Array<>(4);

    void add(Tile tile) {
      tiles.add(tile);
      probability += tile.rarity;
    }
  }
}
